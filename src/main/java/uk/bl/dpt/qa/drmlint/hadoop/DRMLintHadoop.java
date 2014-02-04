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
package uk.bl.dpt.qa.drmlint.hadoop;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.NLineInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import uk.bl.dpt.qa.drmlint.CheckResult;
import uk.bl.dpt.qa.drmlint.DRMLint;
import uk.bl.dpt.qa.drmlint.Tools;
import uk.bl.dpt.qa.drmlint.converter.PDFToText;

/**
 * 20140113 - increased client heap size to 2gb as getting heap space errors
 * 
 * @author wpalmer
 */
public class DRMLintHadoop extends Configured implements Tool {

	/**
	 * Process the pdf/epub
	 * @author wpalmer
	 */
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable> {

		private static FileSystem gFS = null;
		private static File gTempDir = null;
		private static DRMLint gLint = null;
		private static Path gOutputDir = null;
		
		@Override
		public void configure(JobConf job) {
			// TODO Auto-generated method stub
			super.configure(job);
			try {
				gFS = FileSystem.get(job);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			gTempDir = Tools.newTempDir();
			
			gLint = new DRMLint();
			
			gOutputDir = new Path(job.get("mapred.output.dir"));
			
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			super.close();
			
			// clean up temp dir
			Tools.deleteDirectory(gTempDir);
			
		}

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, LongWritable> collector, Reporter reporter)
						throws IOException {

			try {
				// load file from HDFS
				Path hdfsFile = new Path(value.toString());
				File filePDF = new File(gTempDir.getAbsolutePath()+"/"+hdfsFile.getName());
				File fileTXT = new File(filePDF.getAbsolutePath()+".txt");
				File fileXML = new File(filePDF.getAbsolutePath()+".report.xml");
				File fileZIP = new File(filePDF.getAbsolutePath()+".zip");
				
				List<String> generatedFiles = new LinkedList<String>();
				HashMap<String, String> checksums = new HashMap<String, String>();
				
				gFS.copyToLocalFile(hdfsFile, new Path(filePDF.getAbsolutePath()));
				
				boolean extractText = true;
				boolean runDRMLint = true;
				boolean textExtractSuccess = true;
				
				//very simple workflow here - extract text from a PDF, generate checksum, zip the files
				if(extractText) {
					textExtractSuccess = PDFToText.process(filePDF, fileTXT);
					if(fileTXT.exists()) {
						generatedFiles.add(fileTXT.getName());
						checksums.put(fileTXT.getName(), Tools.generateChecksum(fileTXT.getAbsolutePath()));			
						System.out.println("txt: "+fileTXT.getAbsolutePath()+" size: "+fileTXT.length());
					}
				}
				
				if(runDRMLint) {
					List<CheckResult> results = gLint.check(filePDF);
					PrintWriter pw = new PrintWriter(new FileWriter(fileXML));
					DRMLint.printResults(results, pw);
					pw.close();
					generatedFiles.add(fileXML.getName());			
					checksums.put(fileXML.getName(), Tools.generateChecksum(fileXML.getAbsolutePath()));
					System.out.println("xml: "+fileXML.getAbsolutePath()+" size: "+fileXML.length());
				}

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
				
				collector.collect(new Text(fileZIP.getName()), new LongWritable(1));
				
			} catch(Exception e) {
				collector.collect(new Text("Exception: "+value.toString()+" "+e.getMessage()), new LongWritable(0));
			}

		}

	}
	
	/**
	 * Generate an overall report csv
	 * @author wpalmer
	 */
	public static class Reduce extends MapReduceBase implements Reducer<Text, LongWritable, Text, Text> { 

		private static FileSystem gFS = null;
		private static String gInputDir = null;
		
		@Override
		public void configure(JobConf job) {
			// TODO Auto-generated method stub
			super.configure(job);
			try {
				gFS = FileSystem.get(job);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			gInputDir = job.get("mapred.output.dir");
			
		}
		
		private static InputStream recoverStreamThatEndsWith(InputStream zipfile, String file) throws IOException {
			ZipInputStream zip = new ZipInputStream(zipfile);
			ZipEntry ze = null;
			while(zip.available()>0) {
				ze = zip.getNextEntry();
				if(ze!=null) {
					if(ze.getName().endsWith(file)) {
						return zip;
					}
				}
			}
			return null;
		}
		
		@Override
		public void reduce(Text key, Iterator<LongWritable> values,
				OutputCollector<Text, Text> collector, Reporter reporter)
						throws IOException {

			if(key.toString().startsWith("Exception")) {
				collector.collect(key, new Text(""));
			} else {
			
				InputStream zipStream = gFS.open(new Path(gInputDir+"/"+key));
				if(zipStream!=null) {
					InputStream report = recoverStreamThatEndsWith(zipStream, ".xml");
					if(report!=null) {
						BufferedInputStream xml = new BufferedInputStream(report);
						if(xml!=null) {
							xml.mark(1024768);
							String valid = Tools.getXpathVal(new CloseShieldInputStream(xml), "/drmlint/check/valid/@result");
							xml.reset();
							String drm = Tools.getXpathVal(new CloseShieldInputStream(xml), "/drmlint/check/drm/@result");
							xml.close();
							collector.collect(key, new Text("\t,"+valid+","+drm+","));
							return;
						} 
					}
				}

				collector.collect(new Text(key), new Text("Failure"));
				
			}
			
		}

	}

	/* (non-Javadoc)
	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
	 */
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		JobConf conf = new JobConf(DRMLintHadoop.class);
		
		// String to use for name and output folder in HDFS
		String name = "DRMLintHadoop_"+System.currentTimeMillis();
		
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(name));
		
		conf.setJobName(name);
		
		//set the mapper to this class' mapper
		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);
		
		//this input format should split the input by one line per map by default.
		conf.setInputFormat(NLineInputFormat.class);
		conf.setInt("mapred.line.input.format.linespermap", 200);
		
		//sets how the output is written cf. OutputFormat
		conf.setOutputFormat(TextOutputFormat.class);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(LongWritable.class);
		
		//we only want one reduce task
		conf.setNumReduceTasks(28);
		
		JobClient.runJob(conf);
		
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ToolRunner.run(new DRMLintHadoop(), args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
