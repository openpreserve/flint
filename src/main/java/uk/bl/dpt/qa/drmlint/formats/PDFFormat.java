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
import java.util.Scanner;

import org.apache.log4j.Logger;

import uk.bl.dpt.qa.drmlint.wrappers.CalibreWrapper;
import uk.bl.dpt.qa.drmlint.wrappers.ExiftoolWrapper;
import uk.bl.dpt.qa.drmlint.wrappers.Jhove1Wrapper;
import uk.bl.dpt.qa.drmlint.wrappers.Jhove2Wrapper;
import uk.bl.dpt.qa.drmlint.wrappers.PDFBoxWrapper;

/**
 * @author wpalmer
 *
 */
public class PDFFormat implements Format {

	@SuppressWarnings("unused")
	private static Logger gLogger = null;
	
	/**
	 * Create a new PDFFormat object
	 * @param pLogger logger to use
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
	
	//LockLizard and HYPrLock encapsulate pdfs in to a different drm-ed format
	
	//Seems most PDF DRM uses Adobe Digital Editions
	
	@Override
	public boolean containsDRM(File pPDF) {
		boolean ret = false;
		
		boolean pdfboxa = checkDRMWithApachePDFBoxAbsolute(pPDF); 
		ret |= pdfboxa;
		System.out.print("drm_pdfboxa: "+pdfboxa+", ");
		boolean pdfboxg = checkDRMWithApachePDFBoxGranular(pPDF);
		ret |= pdfboxg;
		System.out.print("drm_pdfboxg: "+pdfboxg+", ");
		boolean naiive = checkDRMNaiive(pPDF);
		ret |= naiive;
		System.out.print("drm_naiive: "+naiive+", ");
		boolean exiftool = checkDRMWithExiftool(pPDF);
		ret |= exiftool;
		System.out.print("drm_exiftool: "+exiftool+", ");
		
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
		return "0.0.4";
	}
	
	@Override
	public boolean isValid(File pFile) {

		boolean ret = false;
		
		boolean pdfbox = isValidPDFBox(pFile); 
		ret |= pdfbox;
		System.out.print("valid_pdfbox: "+pdfbox+", ");
		boolean jhove1 = isValidJhove1(pFile);
		ret |= jhove1;
		System.out.print("valid_jhove1: "+jhove1+", ");
		//ret |= isValidJhove2(pFile);
		//ret |= isValidCalibre(pFile);
		
		//System.gc();

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

	/**
	 * We try and load the pdf into memory and then save it out of memory
	 * this should let us know if the file is valid or not; however, it is slow!
	 */
	private boolean isValidPDFBox(File pFile) {

		boolean ret = false;
		
		ret = PDFBoxWrapper.isValid(pFile);
		
		return ret;

	}	
	
	private boolean isValidJhove1(File pFile) {

		boolean ret = false;
		
		ret = Jhove1Wrapper.isValid(pFile);
		
		return ret;

	}	
	
	@SuppressWarnings("unused")
	private boolean isValidJhove2(File pFile) {

		boolean ret = false;
		
		ret = Jhove2Wrapper.isValid(pFile);
		
		return ret;

	}	
	
	/**
	 * Check for encryption with Apache PDFBox
	 * -> only query one method
	 * @param pPDF pdf file to check
	 * @return
	 */
	private boolean checkDRMWithApachePDFBoxAbsolute(File pPDF) {
		
		boolean ret = false;
		
		ret = PDFBoxWrapper.hasDRM(pPDF);

		return ret;
	}
	
	/**
	 * Check for encryption with Apache PDFBox
	 * -> query the encryption dictionary (might allow more granular checks of protection)
	 * @param pPDF pdf file to check
	 * @return
	 */
	private boolean checkDRMWithApachePDFBoxGranular(File pPDF) {
		
		boolean ret = false;
		
		ret = PDFBoxWrapper.hasDRMGranular(pPDF);
		
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

	private boolean checkDRMWithExiftool(File pPDF) {
		boolean ret = false;
		
		ret = ExiftoolWrapper.hasDRM(pPDF);
		
		return ret;
	}
	
}
