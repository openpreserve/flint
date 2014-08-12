package uk.bl.dpt.qa.flint.epub.checks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.checks.CheckCategory;
import uk.bl.dpt.qa.flint.checks.CheckCheck;
import uk.bl.dpt.qa.flint.checks.TimedTask;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Wrapper around additional specific DRM checks that produces an error message
 * in case of a timing out after EPUBFormat#Wrapper_TIMEOUT seconds.
 */
public class SpecificDrmChecks extends TimedTask {

    private Logger logger;
    private Set<String> patternFilter;

    /**
     * Create a SpeficDRMChecks Object that times out if calls take longer than expected
     *
     * @param timeout timeout to use
     * @param patternFilter a set of strings indicating which categories to use and not
     */
    public SpecificDrmChecks(long timeout, Set<String> patternFilter) {
        super(FixedCategories.NO_DRM_RIGHTS_FILE.toString(), timeout);
        this.patternFilter = patternFilter;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public LinkedHashMap<String, CheckCategory> call() throws Exception {
        String catName = FixedCategories.NO_DRM_RIGHTS_FILE.toString();
        LinkedHashMap<String, CheckCategory> cmap = new LinkedHashMap<String, CheckCategory>();
        if (patternFilter == null || patternFilter.contains(catName) ) {
            CheckCategory cc = new CheckCategory(catName);
            cc.add(new CheckCheck("checkForRightsFile", !checkForRightsFile(contentFile), null));
            logger.debug(cc.get("checkForRightsFile").toString());
            cmap.put(cc.getName(), cc);
        }
        return cmap;
    }

    /**
     * Check for rights.xml file
     * @param pEPUB the content file
     * @return true if there's a rights.xml file or false
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
