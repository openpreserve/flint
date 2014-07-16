/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Author: William Palmer (William.Palmer@bl.uk)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package uk.bl.dpt.qa.flint.hadoop;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.dpt.qa.flint.Flint;
import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.converter.PDFToText;
import uk.bl.dpt.qa.flint.wrappers.Tools;
import uk.bl.dpt.utils.checksum.ChecksumUtil;
import uk.bl.dpt.utils.util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


/**
 * MapReduce facility for FLint; takes a file with a list of hdfs filepaths as input,
 * each of the files are sent to FLint for checking (map phase) and written out as
 * a row in a tabular data structure (reduce phase). Technically the reduce phase just
 * pipes through the rows creates in the map phase, and it makes sure that for each
 * format there is a header file written.
 *
 * It uses the 'new' API introduced since version 0.20.x.
 *
 */
public class FlintHadoop {

    private static Logger LOGGER = LoggerFactory.getLogger(FlintHadoop.class);
    private static HadoopVersion gHadoopVersion = new Hadoop2_0_0_CDH4_2_0();

    /**
     * Convert a CheckResult to a Text Object for output
     * @author wpalmer
     */
    public static class CheckResultText extends Text {

        /**
         * Construct a Text Object from a CheckResult
         * @param pResult CheckResult to use for output
         */
        public CheckResultText(CheckResult pResult) {
            super(StringUtils.join(pResult.toMap().values(), "\t"));
        }

        /**
         * Construct an empty CheckResultText
         */
        public CheckResultText() {
            super("");
        }

    }
    
    /**
     * Map class
     */
    public static class FlintMap extends Mapper<LongWritable, Text, Text, CheckResultText> {

        // TODO: deal with these debug switches
        private static final boolean extractText = false;
        private static final boolean runFLint = true;
        private static final boolean zipFlint = false;
        private static boolean textExtractSuccess = false;

        private FileSystem gFS = null;
        private File gTempDir = null;
        private Flint gLint = null;
        private Path gOutputDir = null;

        @Override
        public void setup(Context pContext) throws IOException, InterruptedException {
            super.setup(pContext);
            try {
                gFS = FileSystem.get(pContext.getConfiguration());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            gTempDir = Tools.newTempDir();
            LOGGER.info("created new tempDir {} and it exists: {}", gTempDir, gTempDir.exists());

            try {
                gLint = new Flint();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

            gOutputDir = new Path(pContext.getConfiguration().get("mapred.output.dir"));

        }


        @Override
        public void cleanup(Context pContext) throws IOException, InterruptedException {
            super.cleanup(pContext);

            // clean up temp dir
            if (gTempDir.exists()) {
                FileUtil.deleteDirectory(gTempDir);
            }

        }

        @Override
        public void map(LongWritable pKey, Text pValue, Context pContext) throws IOException, InterruptedException {

            try {
                // load file from HDFS
                Path hdfsFile = new Path(pValue.toString());
                File filePDF = new File(gTempDir.getAbsolutePath()+"/"+hdfsFile.getName());

                List<String> generatedFiles = new LinkedList<String>();
                HashMap<String, String> checksums = new HashMap<String, String>();

                // if on a windows (for testing purposes) we have to useRawLocalFileSystem=true
                // otherwise we get 'Cannot run program "cygpath"' (?)
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    gFS.copyToLocalFile(false, hdfsFile, new Path(filePDF.getAbsolutePath()), true);
                    LOGGER.debug("Copying {} to {}", hdfsFile, filePDF.getAbsolutePath());
                } else {
                    gFS.copyToLocalFile(hdfsFile, new Path(filePDF.getAbsolutePath()));
                    LOGGER.debug("Copying {} to {}", hdfsFile, filePDF.getAbsolutePath());
                }
                LOGGER.info("filePDF exists: {}", filePDF.exists());

                File fileTXT = null;
                //very simple workflow here - extract text from a PDF, generate checksum, zip the files
                if(extractText) {
                    fileTXT = new File(filePDF.getAbsolutePath()+".txt");
                    textExtractSuccess = PDFToText.process(filePDF, fileTXT);
                    generatedFiles.add(fileTXT.getName());
                    checksums.put(fileTXT.getName(), ChecksumUtil.generateChecksum(fileTXT.getAbsolutePath()));
                    LOGGER.debug("txt: {}, size: {}", fileTXT.getAbsolutePath(), fileTXT.length());
                }

                List<CheckResult> results;
                if(runFLint) {
                    results = gLint.check(filePDF);
                    if (zipFlint) {
                        File fileXML = new File(filePDF.getAbsolutePath()+".report.xml");
                        File fileZIP = new File(filePDF.getAbsolutePath()+".zip");
                        PrintWriter pw = new PrintWriter(new FileWriter(fileXML));
                        Flint.printResults(results, pw);
                        pw.close();
                        generatedFiles.add(fileXML.getName());
                        checksums.put(fileXML.getName(), ChecksumUtil.generateChecksum(fileXML.getAbsolutePath()));
                        LOGGER.debug("xml: {}, size: {}", fileXML.getAbsolutePath(), fileXML.length());
                        Tools.zipGeneratedFiles(textExtractSuccess, checksums, generatedFiles, fileZIP.getAbsolutePath(), gTempDir.getAbsolutePath()+"/");
                        // clean up
                        if(filePDF.exists()) filePDF.delete();
                        if(fileXML.exists()) fileXML.delete();
                        if(fileTXT.exists()) fileTXT.delete();
                        if(fileZIP.exists()) {
                            // store zip file
                            gFS.copyFromLocalFile(new Path(fileZIP.getAbsolutePath()), gOutputDir);
                            fileZIP.delete();
                        }
                    }
                }

                pContext.write(new Text(results.get(0).getFilename()), new CheckResultText(results.get(0)));

            } catch(Exception e) {
                // TODO: is this what we want?:
                LOGGER.error("Caught Exception: {}", e);
                pContext.write(new Text("Exception: " + pValue.toString() + " " + e.getMessage()), new CheckResultText());
            }

        }

    }

    /**
     * Generate an overall report csv
     */
//    public static class FlintReduce extends Reducer<Text, CheckResultText, Text, Text> {
//
//        private FileSystem gFS = null;
//        private Path outputDir = null;
//        private File gTempDir = null;
//
//        @Override
//        public void setup(Context pContext) throws IOException, InterruptedException {
//            super.setup(pContext);
//            try {
//                gFS = FileSystem.get(pContext.getConfiguration());
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            gTempDir = Tools.newTempDir();
//            LOGGER.debug("created new tempDir {} and it exists: {}", gTempDir, gTempDir.exists());
//
//            outputDir = new Path(pContext.getConfiguration().get("mapred.output.dir"));
//
//            try {
//                writeHeader("PDF");
//            } catch (Exception e) {
//                LOGGER.error("caught Exception while trying to write header", e);
//            }
//        }
//
//        @Override
//        public void cleanup(Context pContext) throws IOException, InterruptedException {
//            super.cleanup(pContext);
//            // clean up temp dir
//            if (gTempDir.exists()) {
//            	FileUtil.deleteDirectory(gTempDir);
//            }
//        }
//
//
//        /**
//         * Writes the header of the format-specific tabular data to its own file.
//         *
//         * @param pFormat the string representation of the format (e.g. "PDF", "EPUB",..)
//         * @throws Exception
//         */
//        private void writeHeader(String pFormat) throws Exception {
//            File headerFile = new File(gTempDir, "header_for_" + pFormat);
//            if (!headerFile.exists()) {
//                Path headerFileHDFS = new Path(outputDir, headerFile.getName());
//                if (!gFS.exists(headerFileHDFS)) {
//                    FileUtils.write(headerFile, "filename\t" + FlintHadoop.buildHeader(pFormat));
//                    gFS.copyFromLocalFile(false, false, new Path(headerFile.getAbsolutePath()), headerFileHDFS);
//                }
//            }
//        }
//
//        @Override
//        public void reduce(Text pKey, Iterable<CheckResultText> pValues, Context pContext)
//                throws IOException, InterruptedException {
//
//            if (pKey.toString().startsWith("Exception")) {
//                pContext.write(pKey, new CheckResultText());
//            } else {
//                //InputStream zipStream = gFS.open(new Path(gInputDir+"/"+key));
//                //if(zipStream!=null) {
//                //    InputStream report = recoverStreamThatEndsWith(zipStream, ".xml");
//                //    if(report!=null) {
//                //        BufferedInputStream xml = new BufferedInputStream(report);
//                //        if(xml!=null) {
//                //            xml.mark(1024768);
//                //            String overall = Tools.getXpathVal(new CloseShieldInputStream(xml), "/flint/checkedFile/@result");
//                //            xml.reset();
//                //            String wellFormed = Tools.getXpathVal(new CloseShieldInputStream(xml), "/flint/checkedFile/checkCategory[@name='WELL_FORMED']/@result");
//                //            xml.reset();
//                //            String noDrm = Tools.getXpathVal(new CloseShieldInputStream(xml), "/flint/checkedFile/checkCategory[@name='NO_DRM']/@result");
//                //            xml.close();
//                //            collector.collect(key, new Text("\t,"+overall+","+wellFormed+","+noDrm+","));
//                //            return;
//                //        }
//                //    }
//                for (CheckResultText record : pValues) {
//                    pContext.write(new Text(pKey), record);
//                }
//            }
//        }
//    }

    /**
     * Creates a format-specific header for the tabular output.
     * @param format the string representation of the format (e.g. "PDF", "EPUB",..)
     * @return the header-string as a hadoop-writable Text
     * @throws Exception
     */
    public static Text buildHeader(String format) throws Exception {
        Collection<String> header = new ArrayList<String>();
        header.addAll(Arrays.asList(CheckResult.fixedResultBits));
        header.addAll(new Flint().getFormat(format).getAllCategoryNames());
        return new Text(StringUtils.join(header, "\t"));
    }

    /**
     * Main method
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        // set up the configuration
        // NOTE: apparently it's important to set the conf parameters prior to instantiating
        //       the job with this config, which is somewhere cloned.
        Configuration conf = new Configuration();

        // ***** list of config parameters, verified to work *****
        conf.setInt(gHadoopVersion.linesPerMapKey(), 50);
        // 60 * 60 * 1000ms == 1h; this is very long but necessary for some files :-(
        conf.set(gHadoopVersion.taskTimeoutKey(), Integer.toString(60 * 60 * 1000));

        // set up the job
        // String to use for name and output folder in HDFS
        String name = "FLintHadoop_"+System.currentTimeMillis();
        Job job = new Job(conf, name);
        job.setJarByClass(FlintHadoop.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(name));
        //set the mapper to this class' mapper
        job.setMapperClass(FlintMap.class);
        //job.setReducerClass(FlintReduce.class);
        //this input format should split the input by one line per map by default.
        job.setInputFormatClass(NLineInputFormat.class);
        //sets how the output is written cf. OutputFormat
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CheckResultText.class);
        // TODO: shouldn't the number of allowed tasks be set in the config on the cluster,
        //       as it's sensitive to the hardware setup rather than to this code?
        job.setNumReduceTasks(28);

        job.waitForCompletion(true);
    }
}
