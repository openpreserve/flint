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
package uk.bl.dpt.qa.flint.wrappers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import uk.bl.dpt.utils.checksum.ChecksumUtil;
import uk.bl.dpt.utils.util.FileUtil;
import uk.bl.dpt.utils.util.ResourceUtil;
import uk.bl.dpt.utils.util.StreamUtil;


/**
 * Java wrapper for Exiftool
 * @author wpalmer
 *
 */
public class ExiftoolWrapper {

	//we have to call exiftool by the command line
	/**
	 * Path to Windows binary (in jar)
	 */
	final public static String EXIFTOOL_WINDOWS = "exiftool/exiftool.exe";
	/**
	 * Checksum of Windows binary
	 */
	final public static String EXIFTOOL_WINDOWS_MD5 = "88e8418228f7a5b110a09955c3e7f3b9";
	/**
	 * Path to Linux binary
	 */
	final public static String EXIFTOOL_LINUX = "/usr/bin/exiftool";//path to installed location
	
	private static String EXIFTOOL = null; 
	
	private ExiftoolWrapper() {
		// TODO Auto-generated constructor stub
	}

	private static void setupExiftool() {
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("windows")) {
			try {
				File tempExe = File.createTempFile("exiftool-flint-", ".exe");
				StreamUtil.copyStreamToFile(ResourceUtil.loadResource(EXIFTOOL_WINDOWS), tempExe);
				if(!tempExe.exists()) { 
					System.err.println("ERROR: unable to recover exe from jar");
				} else {
					//check exe here - it is not copying ok!?
					String checksum = ChecksumUtil.generateChecksum("MD5", tempExe.getAbsolutePath()).toLowerCase();
					if(!checksum.equals(EXIFTOOL_WINDOWS_MD5)) {
						System.err.println("ERROR: exiftool checksum mismatch -> "+tempExe.getAbsolutePath());
						System.err.println("       resource: "+EXIFTOOL_WINDOWS_MD5+" copy: "+checksum);
					} else {
						EXIFTOOL=tempExe.getAbsolutePath();
						//System.err.println("Copied exiftool ok -> "+tempExe.getAbsolutePath());
					}
				}
				tempExe.deleteOnExit();//keep file around until we exit so we don't have to copy it again
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		if(os.contains("linux")) {
			if(new File(EXIFTOOL_LINUX).exists()) {
				EXIFTOOL=EXIFTOOL_LINUX;
			} else {
				//panic
				System.err.println("Exiftool not installed? (not at: "+EXIFTOOL_LINUX);
				System.err.println("try \"sudo apt-get install libimage-exiftool-perl\" or equivalent");
			}				
			return;
		}
		if(os.contains("mac")) {
			System.err.println("Not yet set up for Mac");
			//panic
		}
		
	}
	
	/**
	 * Run Exiftool
	 * @param pFile
	 * @return
	 */
	private static File runExiftool(File pFile) {
		List<String> commandLine = Arrays.asList(EXIFTOOL, "-X", pFile.getAbsolutePath());
		ToolRunner runner = new ToolRunner();
		try {
			//System.out.println("Running: "+commandLine.toString());
			//long time = System.currentTimeMillis();
			runner.runCommand(commandLine);
			//System.err.println("Exiftool runtime: "+(System.currentTimeMillis()-time)+"ms");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//do something with stdout buffer
		try {
			File tempXml = File.createTempFile("flint-exiftool-output-", ".xml");
			FileUtil.writeReaderToFile(runner.getStdout(), tempXml);
			//tempXml.deleteOnExit();
			return tempXml;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Does the (PDF) file contain encryption?
	 * @param pFile file to check
	 * @return whether or not the file has encryption
	 */
	public static boolean hasDRM(File pFile) {
		if(null==EXIFTOOL) setupExiftool();
		if(null==EXIFTOOL) {
			//i.e. it's still null so we were unable to set up exiftool environment
			return false;
		}
		
		boolean ret = false;
		
		File output = runExiftool(pFile);
		
		/*
		 * NOTE: we can do more than just detect the presence of DRM with Exiftool (see outputs)
		 * Might want to add more granular approach?
		 */
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(output));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//just try and find the first occurrence of PDF:Encryption
		//i.e. this is an ugly way around rdf namespaces and xpath
		String found = scanner.findWithinHorizon("PDF:Encryption", 0);
		scanner.close();
		
		if(found!=null) {
			//i.e. drm detected
			ret = true;
			//System.out.println("DRM detected with Exiftool");
		}
		
		return ret;
	}

}
