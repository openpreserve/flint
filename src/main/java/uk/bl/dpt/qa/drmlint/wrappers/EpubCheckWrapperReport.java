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

import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.FeatureEnum;

/**
 * This is a skeleton implementation of Report that only checks for the indication of DRM
 * @author wpalmer
 *
 */
public class EpubCheckWrapperReport implements Report {

	private boolean encryption = false;
	
	/**
	 * Return whether or not the validate() call reported encryption of any sort
	 * @return whether or not the validate() call reported encryption of any sort
	 */
	public boolean hasEncryption() {
		return encryption;
	}
	
	/**
	 * Constructor for skeleton implementation
	 */
	public EpubCheckWrapperReport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void error(String arg0, int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exception(String arg0, Exception arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getErrorCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getExceptionCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWarningCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void hint(String arg0, int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void info(String arg0, FeatureEnum arg1, String arg2) {
		//some info about epubcheck encryption check here: http://code.google.com/p/epubcheck/issues/detail?id=16
		//might want to check content type here and ignore some parts of file as in issue above?
		if(FeatureEnum.HAS_ENCRYPTION==arg1) {
			//System.out.println("Encryption!");
			encryption = true;
		}

	}

	@Override
	public void warning(String arg0, int arg1, int arg2, String arg3) {
		// TODO Auto-generated method stub

	}

}
