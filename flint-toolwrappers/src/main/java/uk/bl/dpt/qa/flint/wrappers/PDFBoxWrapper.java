/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Authors: William Palmer (William.Palmer@bl.uk)
 *          Alecs Geuder (Alecs.Geuder@bl.uk)
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

import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.PDEncryptionDictionary;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.apache.pdfbox.preflight.parser.XmlResultParser;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to wrap the Apache PDFBox library
 */
public class PDFBoxWrapper {

    private static Logger LOGGER = LoggerFactory.getLogger(PDFBoxWrapper.class);

    private static Map<String, Element> pseudoCache = new HashMap<String, Element>();

    private static final XmlResultParser parser = new CachingXmlResultParser();

    private PDFBoxWrapper() {}

    /**
     * As preflight is used more than once for different puroposes the result
     * shall be cached for performance reasons.
     */
    private static class CachingXmlResultParser extends XmlResultParser {
        public Element validate (Document rdocument, DataSource source) throws IOException {
            if (pseudoCache.containsKey(source.getName())) {
                // can be null, which means it's not valid
                Element preflight = pseudoCache.get(source.getName());
                // we only cache ONCE and clear after.
                pseudoCache.clear();
                return preflight;
            }
            // state that one has dealt with this file:
            pseudoCache.put(source.getName(), null);

            // now do the actual work that will finish with caching and returning the element
            // in case something goes wrong, the empty entry above will remain testifying we have tried.
            String pdfType = null;
            ValidationResult result = null;
            long before = System.currentTimeMillis();
            PreflightDocument document = null;
            try {
                LOGGER.debug("Beginning the preflight validation.. of {}", source.getName());
                PreflightParser parser = new PreflightParser(source);
                parser.parse();
                LOGGER.debug("get-preflight-document");
                document = parser.getPreflightDocument();
                LOGGER.debug("doc.validate");
                document.validate();
                LOGGER.debug("get-spec-get-fname");
                pdfType = document.getSpecification().getFname();
                LOGGER.debug("get-result");
                result = document.getResult();
            } catch (SyntaxValidationException e) {
                result = e.getResult();
            } finally {
                if (document != null) document.close();
            }
            long after = System.currentTimeMillis();

            LOGGER.debug("generate-response-skeleton");
            Element preflight = generateResponseSkeleton(rdocument, source.getName(), after-before);
            if (result != null && result.isValid()) {
                // valid ?
                Element valid = rdocument.createElement("isValid");
                valid.setAttribute("type", pdfType);
                valid.setTextContent("true");
                preflight.appendChild(valid);
            } else {
                // valid ?
                createResponseWithError(rdocument, pdfType, result, preflight);
            }
            pseudoCache.put(source.getName(), preflight);
            return preflight;
        }
    }

    /**
     * Runs preflight over the pdf file and produces an output file.
     * If the transformation of the preflight output Element to xml
     * @param pFile the input file
     * @return the output-stream of the preflight validation (null if errors occurred).
     * @throws IOException
     * @throws TransformerException 
     */
    public static ByteArrayOutputStream preflightToXml(File pFile) throws IOException, TransformerException {
        Element result = parser.validate(new FileDataSource(pFile));
        LOGGER.debug("generating xml from preflight generated element for {}", pFile);
        Document doc = result.getOwnerDocument();
        doc.appendChild(result);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output;
    }

    /**
     * A better PDFBox isValid() method
     * @param pFile file to check
     * @return true if valid, false if not
     */
    public static boolean isValid(File pFile) {
        try {
            if (parser.validate(new FileDataSource(pFile)) == null) {
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn("IOException leads to invalidity: {}", e);
            return false;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("IllegalArgumentException leads to invalidity: {}", e);
            return false;
        } catch (Exception e) {
            LOGGER.warn("Exception leads to invalidity: {}", e);
            return false;
        }

        // if preflight passes the file then try and extract the text from the file
        // this should be more robust at finding errors than load/save but it's
        // still not ideal
        File pTemp;
        try {
            pTemp = File.createTempFile("flint-temp-", ".pdfbox.txt");
            pTemp.deleteOnExit();
        } catch (IOException e) {
            return false;
        }
        return true;
        //return extractTextFromPDF(pFile, pTemp, true);
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
			File temp = File.createTempFile("flint-temp-"+pFile.getName()+"-", ".pdf");
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
			File tmp = File.createTempFile("flint-", ".tmp");
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
			File tmp = File.createTempFile("flint-", ".tmp");
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

		} catch (Exception e) {
           LOGGER.warn("Exception while doing granular DRM checks leads to invalidity: {}", e);
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
        PDDocument doc = null;
        PrintWriter out = null;
		try {
			PDFTextStripper ts = new PDFTextStripper();
			out = new PrintWriter(new FileWriter(pOutput));
			boolean skipErrors = true;
			doc = PDDocument.load(pFile.toURI().toURL(), skipErrors);
			ts.setForceParsing(skipErrors);
			ts.writeText(doc, out);
			// TODO: extract text from embedded files?
			return true;
		} catch (OutOfMemoryError e) {
            LOGGER.error("out of memory error while trying to extract text from file {}! : {}", pFile.getName(), e);
            System.gc();
        } catch (Exception e) {
			// TODO Auto-generated catch block
            LOGGER.error("caught Exception: {}", e);
			e.printStackTrace();
		} finally {
            try {
                out.close();
                if (doc != null) doc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		return false;

	}

}
