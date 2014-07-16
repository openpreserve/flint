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

import uk.bl.dpt.qa.flint.checks.*;
import uk.bl.dpt.qa.flint.formats.Format;
import uk.bl.dpt.qa.flint.formats.PolicyAware;
import uk.bl.dpt.qa.flint.pdf.checks.FixedCategories;
import uk.bl.dpt.qa.flint.pdf.checks.PolicyValidation;
import uk.bl.dpt.qa.flint.pdf.checks.SpecificDrmChecks;
import uk.bl.dpt.qa.flint.pdf.checks.Wellformedness;

import javax.xml.transform.stream.StreamSource;

import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.checks.FixedCategories;
import uk.bl.dpt.qa.flint.checks.PDFPolicyValidation;
import uk.bl.dpt.qa.flint.checks.SpecificDrmChecks;
import uk.bl.dpt.qa.flint.checks.TimedValidation;
import uk.bl.dpt.qa.flint.checks.WellformedTests;
import uk.bl.dpt.utils.util.ResourceUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;


/**
 * PDF implementation of a Format. It uses schematron-based policy validation of apache preflight
 * and some custom add-on checks including jhove(1) and itext.
 *
 * Useful reads on the topic:
 * - http://wiki.mobileread.com/wiki/DRM
 * - http://dion.t-rexin.org/notes/2008/11/17/understanding-the-pdf-format-drm-and-wookies/
 * - http://www.cs.cmu.edu/~dst/Adobe/Gallery/ds-defcon2/ds-defcon.html
 * - http://en.wikipedia.org/wiki/Adobe_Digital_Editions
 * - http://www.cs.cmu.edu/~dst/Adobe/Gallery/anon21jul01-pdf-encryption.txt
 * - http://www.openplanetsfoundation.org/blogs/2013-07-25-identification-pdf-preservation-risks-sequel
 * - http://www.pdfa.org/2011/08/isartor-test-suite/
 * - http://www.pdflib.com/knowledge-base/pdfa/validation-report/
 *
 * Additional notes:
 * - LockLizard and HYPrLock emcapsulate pdfs in to a different drm-ed format
 * - Seems most PDF DRM uses Adobe Digital Editions
 */
public class PDFFormat extends PolicyAware implements Format {

    private final static String SCH_POLICY = "/pdf-policy-validate/pdf_policy_preflight_test.sch";

    // when does a wrapper's task timeout [seconds]
    private final static long WRAPPER_TIMEOUT = 10 * 60;

    @SuppressWarnings("serial")
	@Override
    public Map<String, Map<String, Set<String>>> getFixedCategories() {
        final Set<String> noDRM = new TreeSet<String>() {{
            add("checkDRMPDFBoxAbsolute");
            add("checkDRMPDFBoxGranular");
            add("checkDRMNaiive");
            add("checkDRM_iText");
        }};
        final Map<String, Set<String>> noDRMMap = new TreeMap<String, Set<String>>() {{
            put(FixedCategories.NO_DRM.toString(), noDRM);
        }};
        final Set<String> wellFormed = new TreeSet<String>() {{
            add("isValidPDFBox");
            add("isValid_iText");
            add("isValidJhove1");
        }};
        final Map<String, Set<String>> wellFormedMap = new TreeMap<String, Set<String>>() {{
            put(FixedCategories.WELL_FORMED.toString(), wellFormed);
        }};
        return new TreeMap<String, Map<String, Set<String>>>() {{
            put(FixedCategories.NO_DRM.toString(), noDRMMap);
            put(FixedCategories.WELL_FORMED.toString(), wellFormedMap);
        }};
    }

    @Override
    public Collection<String> getAllCategoryNames() throws Exception {
        Collection<String> cats = new ArrayList<String>();
        cats.addAll(getFixedCategories().keySet());
        // add a potential policy validation error to all category names
        cats.add(FixedCategories.POLICY_VALIDATION.toString());
        cats.addAll(requestPolicyPatternNames(new StreamSource(getPolicy())));
        return cats;
    }

    @Override
    public CheckResult validationResult(File contentFile) {
        CheckResult checkResult;
        try {
            checkResult = new CheckResult(contentFile.getName(), this.getFormatName(), this.getVersion(), getAllCategoryNames());
        } catch (Exception e) {
            throw new RuntimeException("could not initialise check-result! reason: "+e);
        }
        Long startTime = System.currentTimeMillis();

        checkResult.addAll(TimedValidation.validate(new PolicyValidation(WRAPPER_TIMEOUT, patternFilter), contentFile));
        checkResult.addAll(TimedValidation.validate(new SpecificDrmChecks(WRAPPER_TIMEOUT, patternFilter), contentFile));
        checkResult.addAll(TimedValidation.validate(new WellformedTests(WRAPPER_TIMEOUT, patternFilter), contentFile));

        checkResult.setTime(System.currentTimeMillis() - startTime);
        logger.info("all checks done for {}", this.getFormatName());
        return checkResult;
    }

    @Override
    public boolean canCheck(File pFile, String mType) {
        return (canCheck(mType) ||
                pFile.getName().toLowerCase().endsWith(".pdf"));
    }

    @Override
    public boolean canCheck(String mType) {
        return (mType != null && acceptedMimeTypes().contains(mType));
    }

    @Override
    public Collection<String> acceptedMimeTypes() {
        return new HashSet<String>() {{ add("application/pdf"); }};
    }

    @Override
    public String getFormatName() {
        return "PDF";
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public InputStream getPolicy() {
        return PDFFormat.getPolicyInputStream();
    }

    public static InputStream getPolicyInputStream() {
        return PDFFormat.class.getResourceAsStream(SCH_POLICY);
    }

}
