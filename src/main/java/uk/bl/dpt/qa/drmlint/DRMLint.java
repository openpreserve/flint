/*
 * Copyright 2013 The British Library/SCAPE Project Consortium
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
package uk.bl.dpt.qa.drmlint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.bl.dpt.qa.drmlint.formats.EPUBFormat;
import uk.bl.dpt.qa.drmlint.formats.Format;
import uk.bl.dpt.qa.drmlint.formats.PDFFormat;

/**
 * A program to detect potential DRM issues within files
 * @author wpalmer
 *
 */
public class DRMLint {
	
	private static Logger gLogger = Logger.getLogger(DRMLint.class);
	
	class CheckResult {
		private boolean gDRM;
		private boolean gValid;
		private String gFilename;
		private String gFormat;
		private String gVersion;	
		private long gTime;
		public CheckResult(String pFilename, boolean pDRM, boolean pValid, String pFormat, String pVersion, long pTime) {
			gDRM = pDRM;
			gValid = pValid;
			gFilename = pFilename;
			gFormat = pFormat;
			gVersion = pVersion;
			gTime = pTime;
		}
		public boolean isDRM() {
			return gDRM;
		}
		public boolean isValid() {
			return gValid;
		}
		public String getFilename() {
			return gFilename;
		}
		public String getFormat() {
			return gFormat;
		}
		public String getVersion() {
			return gVersion;
		}
		public long getTimeTaken() {
			return gTime;
		}
	}
	
	private List<Format> formats = new LinkedList<Format>();
	
	/**
	 * Create a new DRMLint object, adding an instance of all formats to the format list
	 * for use by check()
	 */
	public DRMLint() {
		formats.add(new PDFFormat(gLogger));
		formats.add(new EPUBFormat(gLogger));
	}
	
	/**
	 * Print a set of results as XML to stdout
	 * @param pResults results to print
	 * @param pOut output PrintWriter
	 */
	public static void printResults(List<CheckResult> pResults, PrintWriter pOut) {
		pOut.println("<drmlint>");

		for(CheckResult result:pResults) {
			pOut.println("     <check format=\""+result.getFormat()+"\" version=\""+result.getVersion()+"\" timeMS=\""+result.getTimeTaken()+"\">");
			pOut.println("          <file>"+result.getFilename()+"</file>");
			pOut.println("          <valid>"+result.isValid()+"</valid>");
			pOut.println("          <drm>"+result.isDRM()+"</drm>");		
			pOut.println("     </check>");
		}
		
		pOut.println("</drmlint>");
	}
	
	//TODO: change the return type to DRMFound to allow for yes/no/unknown
	/**
	 * Check a file for DRM
	 * @param pFile file to check
	 * @return whether or not DRM was detected
	 */
	public List<CheckResult> check(File pFile) {

		boolean drm = false;
		boolean valid = false;
		boolean checked = false;
		
		String mimetype = FormatDetector.getMimetype(pFile);
		
		List<CheckResult> results = new ArrayList<CheckResult>();
		
		for(Format format:formats) {
			if(format.canCheck(pFile, mimetype)) {
				long time = System.currentTimeMillis();
				gLogger.trace("drmlint: "+pFile+" "+format.getFormatName());
				gLogger.trace("Checking DRM");
				drm |= format.containsDRM(pFile);
				gLogger.trace("Checking validity");
				valid |= format.isValid(pFile);
				//calculate time taken and reset time counter
				long timeTaken = (System.currentTimeMillis()-time);
				time = System.currentTimeMillis();
				results.add(new CheckResult(pFile.getAbsolutePath(), drm, valid, format.getFormatName(), format.getVersion(), timeTaken));
				System.out.println(format.getFormatName()+": v"+format.getVersion()+", "+pFile+", drm: "+drm+", valid: "+valid+", time: "+timeTaken+"ms");
				checked = true;
			}
		}
		
		if(!checked) {
			System.out.println("Unable to check: "+pFile+", mimetype: "+mimetype);
		}

		return results;
	}

	private static void traverse(File dir, List<File> files) {
		if(dir.isDirectory()) {
			for(File f:dir.listFiles()) {
				if(f.isDirectory()) {
					traverse(f, files);
				} else {
					files.add(f);
				}
			}
		}
	}
	
	private static void test(DRMLint lint) {
		File testfiledir = new File("src/test/resources/");
		
		List<File> files = new LinkedList<File>();
		traverse(testfiledir, files);
		
		List<CheckResult> results = new ArrayList<CheckResult>();
		
		for(File file:files) {
			results.addAll(lint.check(file));
		}
		
		//printResults(results, System.out);
		
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("results.xml"));
			printResults(results, out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Stub main method
	 * If no arguments will call test() method
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		
		DRMLint lint = new DRMLint();
		
		if(0==args.length) {
			test(lint);
			return;
		}
		
		List<CheckResult> results = new ArrayList<CheckResult>();
		
		for(String file:args) {
			results.addAll(lint.check(new File(file)));
		}
		
		//printResults(results);

	}
	
}
