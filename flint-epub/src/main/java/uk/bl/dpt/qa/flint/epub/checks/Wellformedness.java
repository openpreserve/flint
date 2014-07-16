package uk.bl.dpt.qa.flint.epub.checks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.checks.CheckCategory;
import uk.bl.dpt.qa.flint.checks.CheckCheck;
import uk.bl.dpt.qa.flint.checks.TimedTask;
import uk.bl.dpt.qa.flint.wrappers.CalibreWrapper;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Wrapper around additional specific WellFormdness checks that produces an error message
 * in case of a timing out after EPUBFormat#Wrapper_TIMEOUT seconds.
 */
public class Wellformedness extends TimedTask {

    private Logger logger;
    private Set<String> patternFilter;

    public Wellformedness(long timeout, Set<String> patternFilter) {
        super(FixedCategories.WELL_FORMED_CALIBRE.toString(), timeout);
        this.patternFilter = patternFilter;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public LinkedHashMap<String, CheckCategory> call() throws Exception {
        String catName = FixedCategories.WELL_FORMED_CALIBRE.toString();
        LinkedHashMap<String, CheckCategory> cmap = new LinkedHashMap<String, CheckCategory>();
        if (patternFilter == null || patternFilter.contains(catName) ) {
            CheckCategory cc = new CheckCategory(catName);
            if (CalibreWrapper.calibreIsAvailable()) {
                try {
                    cc.add(new CheckCheck("isValidCalibre", CalibreWrapper.isValid(contentFile), null));
                    logger.debug(cc.get("isValidCalibre").toString());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            cmap.put(cc.getName(), cc);
        }
        return cmap;
    }
}
