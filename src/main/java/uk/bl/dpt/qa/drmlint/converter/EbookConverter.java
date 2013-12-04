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
package uk.bl.dpt.qa.drmlint.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import uk.bl.dpt.qa.drmlint.wrappers.CalibreWrapper;

/**
 * This class converts and validates ebooks
 * @author wpalmer
 *
 */
public class EbookConverter {

	
	// "PDF documents are one of the worst formats to convert from"
	// http://manual.calibre-ebook.com/conversion.html#pdfconversion
	
	private EbookConverter() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Convert an ebook to epub
	 * @param pFile ebook to convert
	 * @return File object for new ebook (in temp space)
	 */
	public static File convertToEpub(File pFile) {
		return CalibreWrapper.convertEbook(pFile, "epub");
	}
	
	private static String fileToString(File pFile) {
		String ret = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(pFile));
			while(reader.ready()) {
				ret+=reader.readLine()+"\n";
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Verify whether or not a migration was successful
	 * @param pOriginal original ebook
	 * @param pNew new ebook
	 * @return true/false whether migration was successful or not 
	 */
	public static boolean verifyMigration(File pOriginal, File pNew) {
		boolean verified = false;
		//convert original to text
		System.out.println("Converting original ebook to text...");
		File oldText = CalibreWrapper.convertEbook(pOriginal, "txt");
		//convert new to text
		System.out.println("Converting new ebook to text...");
		File newText = CalibreWrapper.convertEbook(pNew, "txt");		
		//word diff the two ebooks
		System.out.println("Generating diff...");
		diff_match_patch diffmatchpatch = new diff_match_patch();
		List<Diff> alldiffs = diffmatchpatch.diff_main(fileToString(oldText), fileToString(newText), false);
		List<Diff> diffs = new ArrayList<Diff>();
		//remove diffs where the text is equal
		for(Diff diff:alldiffs) {
			if(Operation.EQUAL!=diff.operation) {
				//if it's just a whitespace diff then ignore it
				if(0!=diff.text.trim().length()) {
					diffs.add(diff);
				}
			} 
		}
		System.out.println("Diff count: "+diffs.size());
		if(0==diffs.size()) {
			verified = true;
		}
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter("diff.txt"));
			for(Diff d:diffs) {
				out.println(d.operation+": "+d.text);
			}
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return verified;
	}
	
}
