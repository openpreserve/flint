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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

/**
 * Class to wrap the Apache PDFBox library
 * @author wpalmer
 *
 */
public class PDFBoxWrapper {

	private PDFBoxWrapper() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Check if a PDF file is valid or not
	 * @param pFile file to check
	 * @return whether the file is valid or not
	 */
	public static boolean isValid(File pFile) {
		boolean ret = false;
		try {
			PDFParser parser = new PDFParser(new FileInputStream(pFile));
			parser.parse();
			File temp = File.createTempFile("drmlint-temp-", ".pdf");
			parser.getPDDocument().save(temp);
			parser.getDocument().close();
			temp.delete();
			ret = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (COSVisitorException e) {
			// TODO Auto-generated catch block
			ret = false;
		}
		return ret;
	}
	
	/**
	 * Check if a PDF file has DRM or not
	 * @param pFile file to check
	 * @return whether the file is had DRM or not
	 */
	public static boolean hasDRM(File pFile) {
		boolean ret = false;
		try {
			PDFParser parser = new PDFParser(new FileInputStream(pFile));
			parser.parse();
			ret = parser.getDocument().isEncrypted();
			parser.getDocument().close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Check for encryption with Apache PDFBox
	 * -> query the encryption dictionary (might allow more granular checks of protection)
	 * @param pPDF pdf file to check
	 * @return whether or not the file has DRM
	 */
	public static boolean hasDRMGranular(File pPDF) {
		
		boolean ret = false;
		
		try {
			PDFParser parser = new PDFParser(new FileInputStream(pPDF));
			parser.parse();
			
			COSDictionary dict = parser.getDocument().getEncryptionDictionary();
			if(dict!=null) {
				
				//print encryption dictionary
//				for(COSName key:dict.keySet()) {
//					System.out.print(key.getName());
//					String value = dict.getString(key);
//					if(value!=null){
//						System.out.println(": "+value);
//					} else {
//						System.out.println(": "+dict.getLong(key));
//					}
//				}
				
				//this feaure in pdfbox is currently broken, see: https://issues.apache.org/jira/browse/PDFBOX-1651
				//AccessPermission perms = parser.getPDDocument().getCurrentAccessPermission();
				//this is a work around; creating a new object from the data
				AccessPermission perms = new AccessPermission(dict.getInt("P"));
				
				boolean debug = false;

				if(debug) {

					System.out.println("canAssembleDocument()        : "+perms.canAssembleDocument());
					System.out.println("canExtractContent()          : "+perms.canExtractContent());
					System.out.println("canExtractForAccessibility() : "+perms.canExtractForAccessibility());
					System.out.println("canFillInForm()              : "+perms.canFillInForm());
					System.out.println("canModify()                  : "+perms.canModify());
					System.out.println("canModifyAnnotations()       : "+perms.canModifyAnnotations());
					System.out.println("canPrint()                   : "+perms.canPrint());
					System.out.println("canPrintDegraded()           : "+perms.canPrintDegraded());
					System.out.println("isOwnerPermission()          : "+perms.isOwnerPermission());
					System.out.println("isReadOnly()                 : "+perms.isReadOnly());

				}
			}
			
			parser.getDocument().close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
}
