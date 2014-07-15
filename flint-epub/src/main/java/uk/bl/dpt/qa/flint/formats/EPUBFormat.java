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
package uk.bl.dpt.qa.flint.formats;

import uk.bl.dpt.qa.flint.checks.CheckCategory;
import uk.bl.dpt.qa.flint.checks.CheckCheck;
import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.checks.FixedCategories;
import uk.bl.dpt.qa.flint.wrappers.CalibreWrapper;
import uk.bl.dpt.qa.flint.wrappers.EpubCheckWrapper;

import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * http://wiki.mobileread.com/wiki/DRM
 * https://github.com/jaketmp/ePub-quicklook
 * Seems most EPUB DRM uses Adobe Content Server (Digital Editions) or Apple FairPlay
 */
public class EPUBFormat extends PolicyAware implements Format {

    private final static String SCH_POLICY = "/epubcheck-policy-validation/minimal.sch";

    @SuppressWarnings("serial")
	@Override
    public Map<String, Map<String, Set<String>>> getFixedCategories() {
        final Set<String> noDRM = new TreeSet<String>() {{
            add("checkForRightsFile");
        }};
        final Map<String, Set<String>> noDRMMap = new TreeMap<String, Set<String>>() {{
            put(FixedCategories.NO_DRM_RIGHTS_FILE.toString(), noDRM);
        }};
        final Set<String> wellFormed = new TreeSet<String>() {{
            add("isValidCalibre");
        }};
        final Map<String, Set<String>> wellFormedMap = new TreeMap<String, Set<String>>() {{
            put(FixedCategories.WELL_FORMED_CALIBRE.toString(), wellFormed);
        }};
        return new TreeMap<String, Map<String, Set<String>>>() {{
            put(FixedCategories.NO_DRM_RIGHTS_FILE.toString(), noDRMMap);
            put(FixedCategories.WELL_FORMED_CALIBRE.toString(), wellFormedMap);
        }};
    }

    @Override
    public Collection<String> getAllCategoryNames() throws Exception {
        Collection<String> cats = new ArrayList<String>();
        cats.addAll(getFixedCategories().keySet());
        // add a potential policy validation error to all category names
        cats.add(FixedCategories.POLICY_VALIDATION.name());
        cats.addAll(requestPolicyPatternNames(new StreamSource(this.getPolicy())));
        return cats;
    }

    @Override
    public CheckResult validationResult(File contentFile) {
        CheckResult checkResult = null;
        
        try {
            checkResult = new CheckResult(contentFile.getName(), this.getFormatName(), this.getVersion(), getAllCategoryNames());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long startTime = System.currentTimeMillis();

        if (patternFilter == null || patternFilter.contains(FixedCategories.NO_DRM_RIGHTS_FILE.toString()) ) {
            checkResult.add(specificDRMChecks(contentFile));
        }
        if (CalibreWrapper.calibreIsAvailable() && (patternFilter == null || patternFilter.contains(FixedCategories.WELL_FORMED_CALIBRE.toString()))) {
            checkResult.add(isWellFormed(contentFile));
        }

        try {
            checkResult.addAll(policyValidationResult(EpubCheckWrapper.check(contentFile), new StreamSource(getPolicy())));
        } catch (Exception e) {
            logger.error("Caught exception: {}", e.getMessage());
        }

        checkResult.setTime(System.currentTimeMillis() - startTime);
        return checkResult;
    }

    /**
     * Run DRM checks against an EPUB file
     * @param pEPUB file to check
     * @return a CheckCategory containing results from specificDRMChecks tests     
     */
    public CheckCategory specificDRMChecks(File pEPUB) {

        CheckCategory cc = new CheckCategory(FixedCategories.NO_DRM_RIGHTS_FILE.toString());

        cc.add(new CheckCheck("checkForRightsFile", !checkForRightsFile(pEPUB), null));
        logger.trace(cc.get("checkForRightsFile").toString());

        return cc;
    }

    @Override
    public boolean canCheck(File pFile, String pMimetype) {
        return (pMimetype.toLowerCase().endsWith("application/epub+zip") ||
                pMimetype.toLowerCase().endsWith("application/x-ibooks+zip") ||
                //simple check
                pFile.getName().toLowerCase().endsWith(".epub") ||
                pFile.getName().toLowerCase().endsWith(".ibooks"));
    }

    @Override
    public String getFormatName() {
    	return "EPUB";
    }

    @Override
    public String getVersion() {
    	return "0.1.0";
    }

    /**
     * This check used EpubCheck - according to it, none of the test files (from Adobe/Google/etc)
     * are valid.  This poses an issue as to what is technically valid and what is ok.
     * NOTE: this uses a new EpubCheck object, as does containsDRM(), so may be able to speed it up
     * but object is on-per-DRMLint, not one-per-input-file.
     * @param pFile file to check
     * @return a CheckCategory containing results from isWellFormed tests
     */
    public CheckCategory isWellFormed(File pFile) {

        CheckCategory cc = new CheckCategory(FixedCategories.WELL_FORMED_CALIBRE.toString());

        try {
            cc.add(new CheckCheck("isValidCalibre", CalibreWrapper.isValid(pFile), null));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        logger.trace(cc.get("isValidCalibre").toString());

        return cc;
    }

    @Override
    public InputStream getPolicy() {
        return getClass().getResourceAsStream(SCH_POLICY);
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
