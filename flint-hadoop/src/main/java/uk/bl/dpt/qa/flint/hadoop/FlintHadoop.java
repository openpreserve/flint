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
import uk.bl.dpt.qa.flint.formats.Format;
import uk.bl.dpt.qa.flint.wrappers.Tools;
import uk.bl.dpt.utils.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * MapReduce facility for Flint; takes a file with a list of hdfs filepaths as input,
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

        private FileSystem gFS = null;
        private File gTempDir = null;
        private Flint gLint = null;
        private Path gOutputDir = null;

        /**
         * Creates a local temporary directory on the node and defines the central
         * hdfs output directory as specified in the configuration.
         *
         * @param pContext the context bound to the mapReduce process
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void setup(Context pContext) throws IOException, InterruptedException {
            super.setup(pContext);
            try {
                gFS = FileSystem.get(pContext.getConfiguration());
            } catch (IOException e) {
                LOGGER.error("IOException while trying to read the configuration from hdfs.");
            }

            gTempDir = Tools.newTempDir();
            LOGGER.info("created new tempDir {} and it exists: {}", gTempDir, gTempDir.exists());

            try {
                gLint = new Flint();
            } catch (IllegalAccessException e) {
                LOGGER.error("IllegalAccessException while trying to instantiate Flint: {}", e);
            } catch (InstantiationException e) {
                LOGGER.error("InstantiationException while trying to instantiate Flint: {}", e);
            }

            gOutputDir = new Path(pContext.getConfiguration().get("mapred.output.dir"));

        }


        /**
         * Deletes the local temporary directory on the node after every {@link #map} call.
         *
         * @param pContext the context bound to the mapReduce process
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void cleanup(Context pContext) throws IOException, InterruptedException {
            super.cleanup(pContext);
            if (gTempDir.exists()) {
                FileUtil.deleteDirectory(gTempDir);
            }

        }

        /**
         * Runs the Flint check on a file at a given `hdfsFilePath` and does additional, optional, format-specific tasks
         *
         * @param pKey parameter unused in the map-phase.
         * @param hdfsFilePath the hdfs path of the file to analise
         * @param pContext the context bound to the mapReduce process
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void map(LongWritable pKey, Text hdfsFilePath, Context pContext) throws IOException, InterruptedException {
            Path hdfsFile = new Path(hdfsFilePath.toString());
            File localFile = new File(gTempDir.getAbsolutePath()+"/"+hdfsFile.getName());

            // if on a windows (for testing purposes) we have to useRawLocalFileSystem=true
            // otherwise we get 'Cannot run program "cygpath"' (?)
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                gFS.copyToLocalFile(false, hdfsFile, new Path(localFile.getAbsolutePath()), true);
                LOGGER.debug("Copying {} to {}", hdfsFile, localFile.getAbsolutePath());
            } else {
                gFS.copyToLocalFile(hdfsFile, new Path(localFile.getAbsolutePath()));
                LOGGER.debug("Copying {} to {}", hdfsFile, localFile.getAbsolutePath());
            }
            LOGGER.info("localFile exists: {}", localFile.exists());

            List<CheckResult> results = gLint.check(localFile);

            for (CheckResult result: results) {
                try {
                    HadoopFormat hadoopFormat = (HadoopFormat) gLint.getFormat(result.getFormat());
                    AdditionalMapTasks additionalTasks = hadoopFormat.getAdditionalMapTasks();
                    if (additionalTasks != null) {
                        additionalTasks.map(gFS, gTempDir, gOutputDir, localFile, results);
                    }
                } catch (ClassCastException e) {
                    throw new RuntimeException("ClassCastException thrown; maybe " + gLint.getClass() +
                            " is no implementation of HadoopFormat? here's the exception: " + e);
                }
            }

            pContext.write(new Text(results.get(0).getFilename()), new CheckResultText(results.get(0)));

        }

    }

    /**
     * Generate an overall report csv
     */
    public static class FlintReduce extends Reducer<Text, CheckResultText, Text, Text> {

        private FileSystem gFS = null;
        private Path outputDir = null;
        private File gTempDir = null;

        @Override
        public void setup(Context pContext) throws IOException, InterruptedException {
            super.setup(pContext);
            try {
                gFS = FileSystem.get(pContext.getConfiguration());
            } catch (IOException e) {
                LOGGER.error("IOException while trying to read the configuration from hdfs.");
            }

            gTempDir = Tools.newTempDir();
            LOGGER.debug("created new tempDir {} and it exists: {}", gTempDir, gTempDir.exists());

            outputDir = new Path(pContext.getConfiguration().get("mapred.output.dir"));

            try {
                writeHeader("PDF");
            } catch (Exception e) {
                LOGGER.error("caught Exception while trying to write header", e);
            }
        }

        /**
         * Deletes the local temporary directory on the node
         *
         * @param pContext the context bound to the mapReduce process
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void cleanup(Context pContext) throws IOException, InterruptedException {
            super.cleanup(pContext);
            if (gTempDir.exists()) {
                FileUtil.deleteDirectory(gTempDir);
            }
        }


        /**
         * Writes the header of the format-specific tabular data to its own file.
         *
         * @param pFormat the string representation of the format (e.g. "PDF", "EPUB",..)
         * @throws Exception
         */
        private void writeHeader(String pFormat) throws Exception {
            File headerFile = new File(gTempDir, "header_for_" + pFormat);
            if (!headerFile.exists()) {
                Path headerFileHDFS = new Path(outputDir, headerFile.getName());
                if (!gFS.exists(headerFileHDFS)) {
                    FileUtils.write(headerFile, "filename\t" + FlintHadoop.buildHeader(pFormat));
                    gFS.copyFromLocalFile(false, false, new Path(headerFile.getAbsolutePath()), headerFileHDFS);
                }
            }
        }

        /**
         * Doesn't do anything by default but pipes the results from the map phase through.
         *
         * @param pKey
         * @param checkResults
         * @param pContext the context bound to the mapReduce process
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void reduce(Text pKey, Iterable<CheckResultText> checkResults, Context pContext)
                throws IOException, InterruptedException {
            if (pKey.toString().startsWith("Exception")) {
                pContext.write(pKey, new CheckResultText());
            } else {
                for (CheckResultText record : checkResults) {
                    pContext.write(new Text(pKey), record);
                }
            }
        }
    }

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
     * @param args the first and only expected item in the String[] is the path to the textfile
     *             containing a list of paths of files to be examined, each path on a single line.
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
        job.setReducerClass(FlintReduce.class);
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
