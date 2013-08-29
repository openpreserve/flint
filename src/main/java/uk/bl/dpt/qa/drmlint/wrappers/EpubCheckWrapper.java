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
package uk.bl.dpt.qa.drmlint.wrappers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

import com.adobe.epubcheck.api.EpubCheck;

/**
 * Wrapper for EpubCheck library
 * @author wpalmer
 *
 */
public class EpubCheckWrapper {

	private EpubCheckWrapper() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Determine if file is valid or not (note EpubCheck is particularly strict; files must adhere to the standard
	 * and most files in the wild probably don't)
	 * @param pFile file to check
	 * @return true/false if valid 
	 */
	public static boolean isValid(File pFile) {
		boolean ret = false;
		
		//buffer outputs so it doesn't spam stdout
		//this won't work with 3.0.1 as WriterReportImpl.fixMessage doesn't properly handle null, like DefaultReportImpl does
		//Fixed in locally installed 3.0.1 (see http://code.google.com/p/epubcheck/issues/detail?id=295)
		PrintWriter pw = new PrintWriter(new ByteArrayOutputStream());
		EpubCheck epubcheck = new EpubCheck(pFile, pw);
		ret = epubcheck.validate();
		pw.close();
		
		return ret;
	}
	
	/**
	 * Check for encryption with EpubCheck
	 * - note that the first call to this method has a long (~10s) startup time, this
	 *   must be epubcheck initialising
	 * @param pFile file to check
	 * @return true/false if contains DRM/encryption
	 */
	public static boolean hasDRM(File pFile) {
		boolean ret = false;
		
		EpubCheckWrapperReport report = new EpubCheckWrapperReport();
		
		EpubCheck epubcheck = new EpubCheck(pFile, report);
		
		epubcheck.validate();
		
		ret = report.hasEncryption();
		
		return ret;
	}
	
}
