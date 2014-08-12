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
package uk.bl.dpt.qa.flint.formats;


import uk.bl.dpt.qa.flint.checks.CheckResult;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Defines an object that can check a particular file format for DRM and validity
 * An instance should be added to DRMLint() so it can be used, that is all that is required for a
 * new file format
 */
public interface Format {

	/**
	 * Can this Format object check this file?
	 * @param pFile file to check
	 * @param pMimetype mimetype of file to check
	 * @return whether or not this Format object can check the file
	 */
	public boolean canCheck(File pFile, String pMimetype);

/**
	 * Can this Format object check this mimeType?
	 * @param pMimetype mimetype of file to check
	 * @return whether or not this Format object can check the file
	 */
	public boolean canCheck(String pMimetype);

    /**
     * @return a collection of mimeTypes accepted by this format
     */
    public Collection<String> acceptedMimeTypes();

    /**
     * Calculate and return the overall validation result for a given file in form of a
     * {@link uk.bl.dpt.qa.flint.checks.CheckResult}.
     *
     * @param contentFile the file to examine
     * @return the result of the validation process
     */
    public CheckResult validationResult(File contentFile);

    /**
     * The check categories in a validation process can be statically defined in code, or
     * possibly 'dynamically' set, as in a configuration file. This method shall define the
     * static, fixed categories.
     * @return  a Map with the category names as keys, and as values a map
     * of all tests assigned to the specific category (category name, set of test names)
     */
    public Map<String, Map<String, Set<String>>> getFixedCategories();

    /**
     * @return all category names expected for the specific instance of a
     * implementation of this format.
     * @throws Exception 
     */
    public Collection<String> getAllCategoryNames() throws Exception;

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
