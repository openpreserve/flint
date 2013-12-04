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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.FeatureEnum;

/**
 * This is a skeleton implementation of Report that stores information as XML
 * @author wpalmer
 *
 */
public class EpubCheckWrapperReportXML implements Report {

	private List<String> warnings = new ArrayList<String>();
	private List<String> errors = new ArrayList<String>();
	private List<String> exceptions = new ArrayList<String>();
	
	/**
	 * Constructor for skeleton implementation
	 */
	public EpubCheckWrapperReportXML() {
		// TODO Auto-generated constructor stub
	}
	
	public static void saveXMLReport(File pFile, boolean valid, EpubCheckWrapperReportXML report, String outputFile) {
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(outputFile));
			
			out.println("<epubcheck>");

			out.println("     <file>"+pFile+"</file>");
			out.println("     <valid>"+valid+"</valid>");
			
			for(String s:report.getWarnings()) {
				out.println(s);
			}
			
			for(String s:report.getErrors()) {
				out.println(s);
			}
			
			for(String s:report.getExceptions()) {
				out.println(s);
			}
			
			out.println("</epubcheck>");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void warning(String resource, int line, int column, String message) {
		String warning = "";
		warning += "     <warning resource=\""+resource+"\" line=\""+line+"\" column=\""+column+"\">\n";
		warning += "          <message>"+message+"</message>\n";
		warning += "     </warning>";
		warnings.add(warning);
	}
	
	/**
	 * Get warnings
	 * @return warnings
	 */
	public List<String> getWarnings() {
		return warnings;
	}
	
	@Override
	public void error(String resource, int line, int column, String message) {
		String error = "";
		error += "     <error resource=\""+resource+"\" line=\""+line+"\" column=\""+column+"\">\n";
		error += "          <message>"+message+"</message>\n";
		error += "     </error>";		
		errors.add(error);
	}

	/**
	 * Get errors
	 * @return errors
	 */
	public List<String> getErrors() {
		return errors;
	}
	
	@Override
	public void exception(String resource, Exception ex) {
		String exception = "";
		exception += "     <exception resource=\""+resource+"\">\n";
		exception += "          <message>"+ex.getMessage()+"<message>\n";
		exception += "     </exception>";
		exceptions.add(exception);
	}

	/**
	 * Get exceptions
	 * @return exceptions
	 */
	public List<String> getExceptions() {
		return exceptions;
	}
	
	@Override
	public int getErrorCount() {
		return errors.size();
	}

	@Override
	public int getExceptionCount() {
		return exceptions.size();
	}

	@Override
	public int getWarningCount() {
		return warnings.size();
	}

	@Override
	public void hint(String resource, int line, int column, String message) {
		// no thanks
	}

	@Override
	public void info(String resource, FeatureEnum feature, String value) {
		// no thanks
	}


}
