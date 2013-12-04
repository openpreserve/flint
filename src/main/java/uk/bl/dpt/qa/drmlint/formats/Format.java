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
import java.util.Map;

/**
 * This interface defines an object that can check a particular file format for DRM and validity
 * An instance should be added to DRMLint() so it can be used, that is all that is required for a
 * new file format
 * 
 * @author wpalmer
 *
 */
public interface Format {

	/**
	 * Does the file contain DRM?
	 * @param pFile file to check
	 * @return whether or not the file contains DRM
	 */
	public Map<String, Boolean> containsDRM(File pFile);

	/**
	 * Can this Format object check this file?
	 * @param pFile file to check
	 * @param pMimetype mimetype of file to check
	 * @return whether or not this Format object can check the file
	 */
	public boolean canCheck(File pFile, String pMimetype);
	
	/**
	 * Checks whether or not this file is a valid/well-formed instance of this format
	 * @param pFile file to check
	 * @return whether or not this file is a valid/well-formed instance of this format
	 */
	public Map<String, Boolean> isValid(File pFile);
	
	/**
	 * Get the name of this Format object
	 * @return the name of this Format object
	 */
	public String getFormatName();
	
	/**
	 * Get the version of this Format object
	 * @return the version of this Format object
	 */
	public String getVersion();
	
}
