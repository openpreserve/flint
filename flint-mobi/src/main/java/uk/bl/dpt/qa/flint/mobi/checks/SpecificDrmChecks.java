package uk.bl.dpt.qa.flint.mobi.checks;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.bl.dpt.qa.flint.checks.CheckCategory;
import uk.bl.dpt.qa.flint.checks.CheckCheck;
import uk.bl.dpt.qa.flint.checks.TimedTask;
import uk.bl.dpt.qa.flint.mobi.checks.FixedCategories;


/**
 * Wrapper around additional specific DRM checks that produces an error message
 * in case of a timing out after MobiFormat#Wrapper_TIMEOUT seconds.
 */
public class SpecificDrmChecks extends TimedTask {
    
    public static final String CHECK_FOR_ENCRYPTION = "checkForEncryption";
    
    private Logger logger;
    private Set<String> patternFilter;

    
    /**
     * Create a SpecificDrmChecks object that times out if calls take longer than expected
     *
     * @param timeout timeout to use
     * @param patternFilter a set of strings indicating which categories to use and not
     */
    public SpecificDrmChecks(long timeout, Set<String> patternFilter) {
        super(FixedCategories.NO_DRM_ENCRYPTION.toString(), timeout);
        
        this.patternFilter = patternFilter;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public LinkedHashMap<String, CheckCategory> call() throws Exception {
        String catName = FixedCategories.NO_DRM_ENCRYPTION.toString();
        LinkedHashMap<String, CheckCategory> cmap = new LinkedHashMap<String, CheckCategory>();
        
        if (patternFilter == null || patternFilter.contains(catName)) {
            CheckCategory cc = new CheckCategory(catName);
            cc.add(new CheckCheck(CHECK_FOR_ENCRYPTION, !checkForEncryption(contentFile), null));
            cmap.put(cc.getName(), cc);
            
            logger.debug(cc.get(CHECK_FOR_ENCRYPTION).toString());
        }
        
        return cmap;
    }

    private boolean checkForEncryption(File contentFile) {
        return false;
    }

}
