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
import java.io.IOException;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 * A class to wrap Apache Tika
 * @author wpalmer
 *
 */
public class TikaWrapper {

	private static final DefaultDetector detector = new DefaultDetector();
	
	private TikaWrapper() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get the mimetype of a file
	 * @param pFile file to check
	 * @return the mimetype of the file
	 */
	public static String getMimetype(File pFile) {
		
		String type = null;

		try {
			TikaInputStream tis = TikaInputStream.get(pFile);
			MediaType mt = detector.detect(tis, new Metadata());
			type = mt.toString();//e.g. "image/tiff"
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return type;
	}
	
}
