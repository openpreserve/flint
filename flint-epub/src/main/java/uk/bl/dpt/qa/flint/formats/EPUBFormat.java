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

import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.checks.TimedValidation;
import uk.bl.dpt.qa.flint.epub.checks.FixedCategories;
import uk.bl.dpt.qa.flint.epub.checks.PolicyValidation;
import uk.bl.dpt.qa.flint.epub.checks.SpecificDrmChecks;
import uk.bl.dpt.qa.flint.epub.checks.Wellformedness;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * http://wiki.mobileread.com/wiki/DRM
 * https://github.com/jaketmp/ePub-quicklook
 * Seems most EPUB DRM uses Adobe Content Server (Digital Editions) or Apple FairPlay
 */
public class EPUBFormat extends PolicyAware implements Format {

    private final static String SCH_POLICY = "/epubcheck-policy-validation/minimal.sch";

    // when does a wrapper's task timeout [seconds]
    private final static long WRAPPER_TIMEOUT = 10 * 60;

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
        CheckResult checkResult;
        try {
            checkResult = new CheckResult(contentFile.getName(), this.getFormatName(), this.getVersion(), getAllCategoryNames());
        } catch (Exception e) {
            throw new RuntimeException("could not initialise check-result! reason: {}", e);
        }
        Long startTime = System.currentTimeMillis();
        checkResult.addAll(TimedValidation.validate(new PolicyValidation(WRAPPER_TIMEOUT, patternFilter), contentFile));
        checkResult.addAll(TimedValidation.validate(new SpecificDrmChecks(WRAPPER_TIMEOUT, patternFilter), contentFile));
        checkResult.addAll(TimedValidation.validate(new Wellformedness(WRAPPER_TIMEOUT, patternFilter), contentFile));
        checkResult.setTime(System.currentTimeMillis() - startTime);
        logger.info("all checks done for {}", this.getFormatName());
        return checkResult;
    }

    @Override
    public boolean canCheck(File pFile, String pMimetype) {
        return (canCheck(pMimetype) ||
                //simple check
                pFile.getName().toLowerCase().endsWith(".epub") ||
                pFile.getName().toLowerCase().endsWith(".ibooks"));
    }

    @Override
    public boolean canCheck(String pMimetype) {
        return acceptedMimeTypes().contains(pMimetype);
    }

    @Override
    public Collection<String> acceptedMimeTypes() {
        return new HashSet<String>() {{
            add("application/epub+zip");
            add("application/x-ibooks+zip");
        }};
    }


    @Override
    public String getFormatName() {
    	return "EPUB";
    }

    @Override
    public String getVersion() {
    	return "0.1.0";
    }

    @Override
    public InputStream getPolicy() {
        return getClass().getResourceAsStream(SCH_POLICY);
    }

    public static InputStream getPolicyStatically() {
        return EPUBFormat.class.getResourceAsStream(SCH_POLICY);
    }
}
