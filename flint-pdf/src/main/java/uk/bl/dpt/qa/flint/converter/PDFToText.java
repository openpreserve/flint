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
package uk.bl.dpt.qa.flint.converter;

import java.io.File;

import uk.bl.dpt.qa.flint.wrappers.PDFBoxWrapper;
import uk.bl.dpt.qa.flint.wrappers.TikaWrapper;
import uk.bl.dpt.qa.flint.wrappers.iTextWrapper;

/**
 * A class that will convert from PDF to text using a variety of different tools
 * @author wpalmer
 *
 */
public class PDFToText {

	
	/**
	 * Process a file from PDF to text 
	 * @param pOriginal original pdf
	 * @param pText new text file
	 * @return if process is successful or not
	 */
	public static boolean process(File pOriginal, File pText) {
		boolean ret = false;
		if(!pOriginal.exists()) return false;
		//test to see if the file is actually a pdf
		if(TikaWrapper.getMimetype(pOriginal).toLowerCase().contains("pdf")) {
			//extract text
			ret = PDFBoxWrapper.extractTextFromPDF(pOriginal, pText, true);
			
			if(!ret) {
				//try and extract text using iText as PDFBox encountered an error
				ret = iTextWrapper.extractTextFromPDF(pOriginal, pText, true);
			}
			
		}
		return ret;
	}
	
}
