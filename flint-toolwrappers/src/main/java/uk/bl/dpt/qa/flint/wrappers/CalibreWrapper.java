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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Wrapper for Calibre - note that there are no static methods here (yet?)
 * @author wpalmer
 *
 */
public class CalibreWrapper {

    static Logger LOGGER = LoggerFactory.getLogger(CalibreWrapper.class);

    private static String gVersion = null;
    private static List<String> CALIBRE_CONVERT = null;

    /**
     * Exception for when Calibre is missing 
     */
    public static class CalibreMissingException extends Exception {
		private static final long serialVersionUID = 733393948514391781L;
		private static String message = ("Can't run Calibre as it is not available; " +
                                  "this method should not be run without Calibre being installed");
        /**
         * Create a default CalibreMissingException
         */
        public CalibreMissingException() {
            super(message);
        }
    }

    /**
     * Don't allow external instantiation, follow the Singleton pattern.
     */
    private CalibreWrapper() {}

    static {
        // TODO: make pathToCalibre a property or a command-line arg?
        @SuppressWarnings("serial")
		Map<String, String> osMap = new HashMap<String, String>() {{
            put("windows", "c:/bin/calibre/calibre2/" + "ebook-convert.exe");
            put("linux",  "/usr/bin/" + "ebook-convert");
        }};

        String os = System.getProperty("os.name").toLowerCase();

        boolean wrongOs = true;
        // find calibre for the current os.
        for (String osBit : osMap.keySet()) {
            if (os.contains(osBit)) {
                String calibrePath = osMap.get(osBit);
                if (new File(calibrePath).exists()) {
                    CALIBRE_CONVERT = Arrays.asList(calibrePath);
                } else {
                    LOGGER.warn("Calibre not installed? (not at: {})", calibrePath);
                    wrongOs = false;
                }
            }
        }
        // nothing found for this os?
        if (CALIBRE_CONVERT != null && wrongOs) {
            LOGGER.warn("Calibre not yet set up for os: {}", os);
        }
    }

    /**
     * Checks whether Calibre is available
     * @return true if true, false if false :-)
     */
    public static boolean calibreIsAvailable() {
        return CALIBRE_CONVERT != null;
    }

    /**
     * Initialise the Calibre version number
     * @throws CalibreMissingException
     */
	private static void getVer() throws CalibreMissingException {
        if (CALIBRE_CONVERT == null && !calibreIsAvailable()) {
            throw new CalibreMissingException();
        }
		//we need to redirect stderr to stdout otherwise bad things happen if drm is detected and stderr is written to first
		ToolRunner runner = new ToolRunner(true);
		try {
			List<String> commandLine = new ArrayList<String>();
			commandLine.addAll(CALIBRE_CONVERT);
			commandLine.add("--version");
			runner.runCommand(commandLine);
			String ver = runner.getStdout().readLine();
			gVersion = ver;//ver.substring(ver.indexOf("(")+1, ver.indexOf(")"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the version of Calibre
	 * @return version string from Calibre
	 * @throws CalibreMissingException in case Calibre is missing
	 */
	public static String getVersion() throws CalibreMissingException {
		if(null==gVersion) getVer();
		return gVersion;
	}
	
	/**
	 * Convert an ebook to another format
	 * @param pOriginal original file
	 * @param pType type to convert to (e.g. "epub" or "mobi")
	 * @return File for converted ebook (or null if error)
	 * @throws CalibreMissingException in case Calibre is missing
	 */
	public static File convertEbook(File pOriginal, String pType) throws CalibreMissingException {
        if (CALIBRE_CONVERT == null && !calibreIsAvailable()) {
            throw new CalibreMissingException();
        }
		//we need to redirect stderr to stdout otherwise bad things happen if drm is detected and stderr is written to first
		ToolRunner runner = new ToolRunner(true);
		try {
			File newEbook = File.createTempFile(pOriginal.getName()+"-", "."+pType);
			List<String> commandLine = new ArrayList<String>();
			commandLine.addAll(CALIBRE_CONVERT);
			commandLine.add(pOriginal.getAbsolutePath());
			commandLine.add(newEbook.getAbsolutePath());
			int exitCode = runner.runCommand(commandLine);
			if(exitCode!=0) return null;
//			System.out.println("Stdout:");
//			BufferedReader o = runner.getStdout();
//			while(o.ready()) System.out.println(o.readLine());
			return newEbook;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Check to see if the file is valid by trying to convert to text
	 * @param pFile file to check
	 * @return true if valid (i.e. can be converted to text)
	 * @throws CalibreMissingException in case Calibre is missing
	 */
	public static boolean isValid(File pFile) throws CalibreMissingException {
        if (CALIBRE_CONVERT == null && !calibreIsAvailable()) {
            throw new CalibreMissingException();
        }

		boolean ret = false;

		//we need to redirect stderr to stdout otherwise bad things happen if drm is detected and stderr is written to first
		ToolRunner runner = new ToolRunner(true);
		try {
			File newEbook = File.createTempFile(pFile.getName()+"-", ".txt");
			newEbook.deleteOnExit();
			List<String> commandLine = new ArrayList<String>();
			commandLine.addAll(CALIBRE_CONVERT);
			commandLine.add(pFile.getAbsolutePath());
			commandLine.add(newEbook.getAbsolutePath());
			int exitCode = runner.runCommand(commandLine);
			if(exitCode!=0) return false;
			BufferedReader o = runner.getStdout();
			while(o.ready()) {
				String line = o.readLine();
				//System.out.println(line);
				//this doesn't seem to work 100% - down to saving of buffer?
				if(line.contains("Output saved to")) {
					ret = true;
				}
				if(line.contains("TXT output written to")) {
					ret = true;
				}
//				if(line.toLowerCase().contains("drmerror")) {
//					return false;
//				}
			}
			
			return ret;
        } catch (Exception e) {
            LOGGER.error("Exception while trying to validate with Calibre: {}", e);
        }
		return ret;
	}
	
	/**
	 * Extracts text from a PDF
	 * @param pFile input file
	 * @param pOutput output file
	 * @param pOverwrite whether or not to overwrite an existing output file
	 * @return true if converted ok, otherwise false
	 * @throws CalibreMissingException in case Calibre is missing
	 */
	public static boolean extractTextFromPDF(File pFile, File pOutput, boolean pOverwrite) throws CalibreMissingException {
		if(pOutput.exists()&(!pOverwrite)) return false;
		//calibre uses the target file extension to decide how to convert the file
		//as we want text, only allow that extension
		if(!pOutput.getName().toLowerCase().endsWith(".txt")) return false;
        if (CALIBRE_CONVERT == null && !calibreIsAvailable()) {
            throw new CalibreMissingException();
        }
		//we need to redirect stderr to stdout otherwise bad things happen if drm is detected and stderr is written to first
		ToolRunner runner = new ToolRunner(true);
		try {
			List<String> commandLine = new ArrayList<String>();
			commandLine.addAll(CALIBRE_CONVERT);
			commandLine.add(pFile.getAbsolutePath());
			commandLine.add(pOutput.getAbsolutePath());
			int exitCode = runner.runCommand(commandLine);
			return exitCode == 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
