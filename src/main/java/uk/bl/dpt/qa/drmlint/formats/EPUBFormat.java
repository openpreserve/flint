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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import uk.bl.dpt.qa.drmlint.wrappers.CalibreWrapper;
import uk.bl.dpt.qa.drmlint.wrappers.EpubCheckWrapper;

/**
 * @author wpalmer
 *
 */
public class EPUBFormat implements Format {

	private static Logger gLogger = null;
	
	/**
	 * Create a new EPUBFormat object to check epub files
	 * @param pLogger Logger to use
	 */
	public EPUBFormat(Logger pLogger) {
		gLogger = pLogger;
	}
	
	//http://wiki.mobileread.com/wiki/DRM
	//https://github.com/jaketmp/ePub-quicklook
	
	//Seems most EPUB DRM uses Adobe Digital Editions or Apple FairPlay
		
	@Override
	public Map<String, Boolean> containsDRM(File pEPUB) {
		
		Map<String, Boolean> ret = new HashMap<String, Boolean>();
		
		String test = "checkWithEpubCheck"; 
		ret.put(test, EpubCheckWrapper.hasDRM(pEPUB));
		gLogger.trace(test+": "+ret.get(test));
		test = "checkForRightsFile";
		ret.put(test, checkForRightsFile(pEPUB));
		gLogger.trace(test+": "+ret.get(test));
		
		//System.gc();
		
		return ret;
	}
	
	@Override
	public boolean canCheck(File pFile, String pMimetype) {
		
		if(pMimetype.toLowerCase().endsWith("application/epub+zip")) {
			return true;
		}		

		if(pMimetype.toLowerCase().endsWith("application/x-ibooks+zip")) {
			return true;
		}		
		
		//simple check
		if(pFile.getName().toLowerCase().endsWith(".epub")) {
			return true;
		}
		
		if(pFile.getName().toLowerCase().endsWith(".ibooks")) {
			return true;
		}

		return false;
		
	}

	@Override
	public String getFormatName() {
		return "EPUB";
	}
	
	@Override
	public String getVersion() {
		return "0.0.6";
	}
	
	/**
	 * This check used EpubCheck - according to it, none of the test files (from Adobe/Google/etc)
	 * are valid.  This poses an issue as to what is technically valid and what is ok.
	 * NOTE: this uses a new EpubCheck object, as does containsDRM(), so may be able to speed it up
	 * but object is on-per-DRMLint, not one-per-input-file. 
	 */
	@Override
	public Map<String, Boolean> isValid(File pFile) {
		
		Map<String, Boolean> ret = new HashMap<String, Boolean>();

		String test = "isValidEpubCheck"; 
		ret.put(test, EpubCheckWrapper.isValid(pFile)); 
		gLogger.trace(test+": "+ret.get(test));
		test = "isValidCalibre";
		ret.put(test, CalibreWrapper.isValid(pFile));
		gLogger.trace(test+": "+ret.get(test));
		
		//System.gc();

		return ret;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// Private methods for this class
	///////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Check for rights.xml file
	 * @param pEPUB
	 * @return
	 */
	private boolean checkForRightsFile(File pEPUB) {
		boolean ret = false;
		
		final String RIGHTSFILE = "META-INF/rights.xml";//http://www.idpf.org/epub/30/spec/epub30-ocf.html#sec-container-metainf-rights.xml
		final String ENCFILE = "META-INF/encryption.xml";//http://www.idpf.org/epub/30/spec/epub30-ocf.html#sec-container-metainf-encryption.xml
		
		ZipFile zip;
		try {
			zip = new ZipFile(pEPUB);
			
			ZipEntry entry = zip.getEntry(RIGHTSFILE);
			if(null!=entry) {
				ret = true;
			}
			
			entry = zip.getEntry(ENCFILE);
			if(null!=entry) {
				ret = true;
			}
			
			zip.close();

		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("Rights and/or encryption file: "+ret);
		return ret;
	}




}
