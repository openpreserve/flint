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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PDEncryptionDictionary;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Class to wrap the Apache PDFBox library
 * @author wpalmer
 *
 */
public class PDFBoxWrapper {

	private static Logger gLogger = Logger.getLogger(PDFBoxWrapper.class);
	
	private PDFBoxWrapper() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Run Preflight over the PDF - if a syntax error is detected or an exception is
	 * thrown then return false
	 * @param pFile file to check
	 * @return true if it passes Preflight tests, false if not
	 */
	public static boolean runPreflight(File pFile) {
		boolean ret = false;
		
		try {
			try {
				PreflightParser parser = new PreflightParser(pFile);
				parser.parse();
				PreflightDocument document = parser.getPreflightDocument();
				document.validate();
				ValidationResult result = document.getResult();
				document.close();

				boolean syntaxError = false;
				for(ValidationError ve:result.getErrorsList()) {
					if(ve.getErrorCode().startsWith("1")) {
						System.err.println("Syntax error: "+ve.getErrorCode()+" "+ve.getDetails());
						syntaxError = true;
					}
				}
				if(!syntaxError) {
					ret = true;
				}

			} catch(SyntaxValidationException e) {
				// occurs if a suitable security handler cannot be found - in this case it is better 
				// to fail for manual investigation
				ret = false;			
			} catch (Exception e) {
				//This can cause "FATAL org.apache.hadoop.mapred.Child: Error running child : java.lang.AbstractMethodError"
				//e.printStackTrace();
				ret = false;
			}
		} catch(StackOverflowError e) {
			//Not nice
			gLogger.fatal("StackOverflowError: "+e.getMessage());
		} catch(OutOfMemoryError e) {
			// Panic
			System.gc();
			//Not nice
			gLogger.fatal("OutOfMemoryError: "+e.getMessage());
		}
		return ret;
	}

	/**
	 * A better PDFBox isValid() method
	 * @param pFile file to check
	 * @return true if valid, false if not
	 */
	public static boolean isValid(File pFile) {
		// run with preflight - return false if syntax errors encountered, or exception thrown
		if(!runPreflight(pFile)) {
			return false;
		}
		// if preflight passes the file then try and extract the text from the file
		// this should be more robust at finding errors than load/save but it's
		// still not ideal
		File pTemp = null;
		try {
			pTemp = File.createTempFile("drmlint-temp-", ".pdfbox.txt");
			pTemp.deleteOnExit();
		} catch (IOException e) {
			return false;
		}
		if(extractTextFromPDF(pFile, pTemp, true)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Loads and saves a PDF
	 * @param pFile PDF file to load
	 * @return whether the file loads and saves successfully or not
	 */
	public static boolean loadSavePDF(File pFile) {
		boolean ret = false;
		
		try {
			
			// Note that this test passes files that fail to open in Acrobat
			// The files are saved with the same errors as the original
			// i.e. this is not an effective test for validity
			
			PDFParser parser = new PDFParser(new FileInputStream(pFile));
			parser.parse();
			File temp = File.createTempFile("drmlint-temp-"+pFile.getName()+"-", ".pdf");
			parser.getPDDocument().save(temp);
			parser.getDocument().close();
			temp.deleteOnExit();
			ret = true;
		} catch (Exception e) {
			
			e.printStackTrace();
			
			// See comments in https://issues.apache.org/jira/browse/PDFBOX-1757
			// PDFBox state that these files have errors and their parser is correct
			// The only way to find out that the parser doesn't like it is to catch
			// a general Exception. 
			
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
			System.setProperty("org.apache.pdfbox.baseParser.pushBackSize", "1024768");
			// NOTE: we use loadNonSeq here as it is the latest parser
			// load() and parser.parse() have hung on test files
			File tmp = File.createTempFile("drmlint-", ".tmp");
			tmp.deleteOnExit();
			RandomAccess scratchFile = new RandomAccessFile(tmp, "rw");
			PDDocument doc = PDDocument.loadNonSeq(new FileInputStream(pFile), scratchFile);
			ret = doc.isEncrypted();
			doc.close();
			
		} catch(IOException e) {

			// This may occur when a suitable security handler cannot be found
			if(e.getMessage().contains("BadSecurityHandlerException")) {
				// if this happens then there must be some sort of DRM here
				ret = true;
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			// See comments in https://issues.apache.org/jira/browse/PDFBOX-1757
			// PDFBox state that these files have errors and their parser is correct
			// The only way to find out that the parser doesn't like it is to catch
			// a general Exception.
			
			// If we reach this point then we have no idea of whether the file contains
			// DRM or not.  Return false and hope it is detected elsewhere.
			
			ret = false;
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
			System.setProperty("org.apache.pdfbox.baseParser.pushBackSize", "1024768");
			// NOTE: we use loadNonSeq here as it is the latest parser
			// load() and parser.parse() have hung on test files
			File tmp = File.createTempFile("drmlint-", ".tmp");
			tmp.deleteOnExit();
			RandomAccess scratchFile = new RandomAccessFile(tmp, "rw");
			PDDocument doc = PDDocument.loadNonSeq(new FileInputStream(pPDF), scratchFile);
			
			PDEncryptionDictionary dict = doc.getEncryptionDictionary();
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
				AccessPermission perms = new AccessPermission(dict.getPermissions());//.getInt("P"));
				
				boolean debug = true;

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
			
			doc.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * Extracts text from a PDF.  Note that Tika uses PDFBox so we will just use the library directly and avoid waiting for 
	 * Tika to use the latest version.
	 * Inspired by PDFBox's ExtractText.java
	 * @param pFile input file
	 * @param pOutput output file
	 * @param pOverwrite whether or not to overwrite an existing output file
	 * @return true if converted ok, otherwise false
	 */
	public static boolean extractTextFromPDF(File pFile, File pOutput, boolean pOverwrite) {
		if(pOutput.exists()&(!pOverwrite)) return false;
		try {
			PDFTextStripper ts = new PDFTextStripper();
			PrintWriter out = new PrintWriter(new FileWriter(pOutput));
			PDDocument doc = null;
			boolean skipErrors = true;
			doc = PDDocument.load(pFile.toURI().toURL(), skipErrors);
			ts.setForceParsing(skipErrors);
			ts.writeText(doc, out);
			// TODO: extract text from embedded files?
			out.close();
			doc.close();		
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
	
}
