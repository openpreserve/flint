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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to wrap iText for the purposes of extracting text from PDFs 
 * Initial testing seems to show PDFBox/Calibre doing a better job
 * @author wpalmer
 *
 */
public class iTextWrapper {

    private static Logger LOGGER = LoggerFactory.getLogger(iTextWrapper.class);

    private iTextWrapper() {}

    /**
     * Extracts text from a PDF.
     * @param pFile input file
     * @param pOutput output file
     * @param pOverwrite whether or not to overwrite an existing output file
     * @return true if converted ok, otherwise false
     */
    public static boolean extractTextFromPDF(File pFile, File pOutput, boolean pOverwrite) {
        if(pOutput.exists()&(!pOverwrite)) return false;

        boolean ret = true;

        PrintWriter pw = null;
        PdfReader reader = null;

        try {
            pw = new PrintWriter(new FileWriter(pOutput));
            reader = new PdfReader(pFile.getAbsolutePath());
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextExtractionStrategy strategy;
            for(int i=0;i<reader.getNumberOfPages();i++) {
                try {
                    //page numbers start at 1
                    strategy = parser.processContent((i+1), new SimpleTextExtractionStrategy());
                    //write text out to file
                    pw.println(strategy.getResultantText());
                } catch(ExceptionConverter e) {
                    e.printStackTrace();
                    ret = false;
                    pw.println("iText Exception: Page "+(i+1)+": "+e.getClass().getName()+": "+e.getMessage());
                }
            }
        } catch (IOException e) {
            ret = false;
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (pw != null) pw.close();
            if (reader != null) reader.close();
        }

        return ret;
    }

    /**
     * Check if a PDF file is valid or not
     * @param pFile file to check
     * @return whether the file is valid or not
     */
    public static boolean isValid(File pFile) {

        boolean ret = false;

        PdfReader reader = null;
        try {
            reader = new PdfReader(pFile.getAbsolutePath());
            LOGGER.debug("validating through {} pages of {}", reader.getNumberOfPages(), pFile.getName());
            for(int i=0;i<reader.getNumberOfPages();i++) {
                //page numbers start at 1
                PdfTextExtractor.getTextFromPage(reader, (i+1));
            }
            ret = true;
        } catch (BadPasswordException e) {
            //actually an error???
        } catch (InvalidPdfException e) {
            LOGGER.warn("InvalidPdfException leads to invalidity: {}", e);
        } catch (IOException e) {
            LOGGER.warn("IOException leads to invalidity: {}", e);
        } catch (Exception e) {
            LOGGER.warn("Exception leads to invalidity: {}", e);
        } finally {
            if (reader != null) reader.close();
        }

        return ret;
    }

    /**
     * Check if a PDF file has DRM or not
     * @param pFile file to check
     * @return whether the file is had DRM or not
     */
    public static boolean hasDRM(File pFile) {

        boolean drm = false;

        PdfReader reader = null;
        try {
            reader = new PdfReader(pFile.getAbsolutePath());
            drm = reader.isEncrypted();
        } catch (BadPasswordException e) {
            //assume drm
            drm = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           if (reader != null) reader.close();
        }

        return drm;
    }

}
