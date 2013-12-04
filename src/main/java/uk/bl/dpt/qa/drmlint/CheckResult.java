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
package uk.bl.dpt.qa.drmlint;

import java.util.Map;

/**
 * Contains the results from a check
 * @author wpalmer
 *
 */
public class CheckResult {
	private Map<String, Boolean> gDRM;
	private Map<String, Boolean> gValid;
	private String gFilename;
	private String gFormat;
	private String gVersion;	
	private long gTime;
	/**
	 * New check result
	 * @param pFilename filename checked
	 * @param pDRM map containing drm checks
	 * @param pValid map containing isValid checks
	 * @param pFormat format used to check file
	 * @param pVersion version of format object used to check
	 * @param pTime time taken to run all checks
	 */
	public CheckResult(String pFilename, Map<String, Boolean> pDRM, Map<String, Boolean> pValid, String pFormat, String pVersion, long pTime) {
		gDRM = pDRM;
		gValid = pValid;
		gFilename = pFilename;
		gFormat = pFormat;
		gVersion = pVersion;
		gTime = pTime;
	}
	/**
	 * DRM detected?
	 * @return true/false if DRM detected
	 */
	public boolean isDRM() {
		return gDRM.containsValue(true);
	}
	/**
	 * Is valid?
	 * @return true/false if valid
	 */
	public boolean isValid() {
		return gValid.containsValue(true);
	}
	/**
	 * Get filename
	 * @return filename
	 */
	public String getFilename() {
		return gFilename;
	}
	/**
	 * Get format object used for the checks
	 * @return name of format object
	 */
	public String getFormat() {
		return gFormat;
	}
	/**
	 * Version of format object used for the checks
	 * @return Version of format object used for the checks
	 */
	public String getVersion() {
		return gVersion;
	}
	/**
	 * Get the time taken to execute tests (in ms)
	 * @return time taken to execute tests (in ms)
	 */
	public long getTimeTaken() {
		return gTime;
	}
	/**
	 * Get list of DRM checks that have been run
	 * @return DRM checks that have been run
	 */
	public Map<String, Boolean> getDRMChecks() {
		return gDRM;
	}
	/**
	 * Get list of validity checks that have been run
	 * @return validity checks that have been run
	 */
	public Map<String, Boolean> getIsValidChecks() {
		return gValid;
	}
	public String toString() {
		return getFormat()+": v"+getVersion()+", "+getFilename()+", drm: "+isDRM()+", valid: "+isValid()+", time: "+getTimeTaken()+"ms";
	}
}