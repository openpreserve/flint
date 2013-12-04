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
package uk.bl.dpt.qa.drmlint.formats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import uk.bl.dpt.qa.drmlint.wrappers.CalibreWrapper;
import uk.bl.dpt.qa.drmlint.wrappers.Jhove1Wrapper;
import uk.bl.dpt.qa.drmlint.wrappers.Jhove2Wrapper;
import uk.bl.dpt.qa.drmlint.wrappers.PDFBoxWrapper;
import uk.bl.dpt.qa.drmlint.wrappers.iTextWrapper;

/**
 * @author wpalmer
 *
 */
public class PDFFormat implements Format {

	private static Logger gLogger = null;
	
	/**
	 * Create a new PDFFormat object to check pdf files
	 * @param pLogger Logger to use
	 */
	public PDFFormat(Logger pLogger) {
		gLogger = pLogger;
	}
	
	//http://wiki.mobileread.com/wiki/DRM
	//http://dion.t-rexin.org/notes/2008/11/17/understanding-the-pdf-format-drm-and-wookies/
	//http://www.cs.cmu.edu/~dst/Adobe/Gallery/ds-defcon2/ds-defcon.html
	//http://en.wikipedia.org/wiki/Adobe_Digital_Editions
	//http://www.cs.cmu.edu/~dst/Adobe/Gallery/anon21jul01-pdf-encryption.txt
	//http://www.openplanetsfoundation.org/blogs/2013-07-25-identification-pdf-preservation-risks-sequel
	//http://www.pdfa.org/2011/08/isartor-test-suite/
	//http://www.pdflib.com/knowledge-base/pdfa/validation-report/
	
	//LockLizard and HYPrLock emcapsulate pdfs in to a different drm-ed format
	
	//Seems most PDF DRM uses Adobe Digital Editions
	
	@Override
	public Map<String, Boolean> containsDRM(File pPDF) {

		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		
		String test = "checkDRMPDFBoxAbsolute"; 
		ret.put(test, PDFBoxWrapper.hasDRM(pPDF));
		gLogger.trace(test+": "+ret.get(test));
		test = "checkDRMPDFBoxGranular";
		ret.put(test, PDFBoxWrapper.hasDRMGranular(pPDF));
		gLogger.trace(test+": "+ret.get(test));
		test = "checkDRMNaiive";
		ret.put(test, checkDRMNaiive(pPDF));
		gLogger.trace(test+": "+ret.get(test));
		test = "checkDRM_iText";
		ret.put(test, iTextWrapper.hasDRM(pPDF));
		gLogger.trace(test+": "+ret.get(test));
		//disabled due to exiftool crashing? (Windows) 
		//ret.put("checkDRMExiftool", ExiftoolWrapper.hasDRM(pPDF));
		
		//System.gc();

		return ret;
	}
	
	@Override
	public boolean canCheck(File pFile, String pMimetype) {
	
		if(pMimetype.toLowerCase().endsWith("application/pdf")) {
			return true;
		}		
		
		//simple check
		if(pFile.getName().toLowerCase().endsWith(".pdf")) {
			return true;
		}

		return false;
		
	}
	
	@Override
	public String getFormatName() {
		return "PDF";
	}
	
	@Override
	public String getVersion() {
		return "0.0.6";
	}
	
	@Override
	public Map<String, Boolean> isValid(File pFile) {

		Map<String, Boolean> ret = new HashMap<String, Boolean>();

		String test = "isValidPDFBox"; 
		ret.put(test, PDFBoxWrapper.isValid(pFile));
		gLogger.trace(test+": "+ret.get(test));
		test = "isValid_iText";
		ret.put(test, iTextWrapper.isValid(pFile));
		gLogger.trace(test+": "+ret.get(test));
		
		// Jhove is passing files that should not pass
		// therefore only add a result if it is negative
		boolean jhove = Jhove1Wrapper.isValid(pFile);
		if(false==jhove) {
			ret.put("isValidJhove1", jhove);
		} 

		return ret;

	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Private methods for this class
	///////////////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unused")
	private boolean isValidCalibre(File pFile) {
		boolean valid = CalibreWrapper.isValid(pFile);
		return valid;
	}

	@SuppressWarnings("unused")
	private boolean isValidJhove2(File pFile) {
		boolean ret = false;
		ret = Jhove2Wrapper.isValid(pFile);
		return ret;
	}	
	
	/**
	 * Search for /encrypt in file 
	 * NOTE: this might be found in content but if we're being conservative it might be useful 
	 * @param pPDF
	 * @return
	 */
	private boolean checkDRMNaiive(File pPDF) {
		
		boolean ret = false;
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(pPDF));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//just try and find the first occurrence of /encrypt (note that this might actually be in the content)
		//scanner.findWithinHorizon("/[rR][oO][oO][tT]", 0);
		String found = scanner.findWithinHorizon("/[eE][nN][cC][rR][yY][pP][tT]", 0);
		
		//System.out.println("Scanner found: "+(found!=null?"yes":"no"));
		
		ret = (found!=null&&found.length()>0)?true:false;
		
		scanner.close();

		return ret;
	}

	
}
