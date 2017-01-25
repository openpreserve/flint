package uk.bl.dpt.qa.flint.formats;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.checks.TimedValidation;
import uk.bl.dpt.qa.flint.mobi.checks.FixedCategories;
import uk.bl.dpt.qa.flint.mobi.checks.SpecificDrmChecks;


public class MobiFormat  extends PolicyAware implements Format {
    
    private final static String SCH_POLICY = "/mobicheck-policy-validation/minimal.sch";
    
    // when does a wrapper's task timeout [seconds]
    private final static long WRAPPER_TIMEOUT = 10 * 60;

    @Override
    @SuppressWarnings("serial")
    public Map<String, Map<String, Set<String>>> getFixedCategories() {
        final Set<String> noDRM = new TreeSet<String>() {{
            add("checkForEncryption");
        }};
        final Map<String, Set<String>> noDRMMap = new TreeMap<String, Set<String>>() {{
            put(FixedCategories.NO_DRM_ENCRYPTION.toString(), noDRM);
        }};
        final Set<String> wellFormed = new TreeSet<String>() {{
            add("isValid");
        }};
        final Map<String, Set<String>> wellFormedMap = new TreeMap<String, Set<String>>() {{
            put(FixedCategories.WELL_FORMED.toString(), wellFormed);
        }};
        return new TreeMap<String, Map<String, Set<String>>>() {{
            put(FixedCategories.NO_DRM_ENCRYPTION.toString(), noDRMMap);
            put(FixedCategories.WELL_FORMED.toString(), wellFormedMap);
        }};
    }

    @Override
    public Collection<String> getAllCategoryNames() throws Exception {
        Collection<String> cats = new ArrayList<String>();
        
        cats.addAll(getFixedCategories().keySet());
        // add a potential policy validation error to all category names
        cats.add(FixedCategories.POLICY_VALIDATION.name());
        // cats.addAll(requestPolicyPatternNames(new StreamSource(this.getPolicy())));
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
        
        checkResult.addAll(TimedValidation.validate(new SpecificDrmChecks(WRAPPER_TIMEOUT, patternFilter), contentFile));
        
        checkResult.setTime(System.currentTimeMillis() - startTime);
        logger.info("all checks done for {}", this.getFormatName());
        
        return checkResult;
    }
    
    @Override
    public boolean canCheck(File pFile, String pMimetype) {
        return (canCheck(pMimetype) ||
                //simple check
                pFile.getName().toLowerCase().endsWith(".mobi")
                || pFile.getName().toLowerCase().endsWith(".azw")
                || pFile.getName().toLowerCase().endsWith(".azw3")
                || pFile.getName().toLowerCase().endsWith(".prc")
               );
    }

    @Override
    public boolean canCheck(String pMimetype) {
        return acceptedMimeTypes().contains(pMimetype);
    }
    
    @Override
    @SuppressWarnings("serial")
    public Collection<String> acceptedMimeTypes() {
        return new HashSet<String>() {{
            add("application/x-mobipocket-ebook");
            add("application/vnd.amazon.ebook");
        }};
    }

    @Override
    public String getFormatName() {
        return "MOBI";
    }

    @Override
    public String getVersion() {
        return "0.1.0";
    }

    @Override
    public InputStream getPolicy() {
        return getClass().getResourceAsStream(SCH_POLICY);
    }

}
