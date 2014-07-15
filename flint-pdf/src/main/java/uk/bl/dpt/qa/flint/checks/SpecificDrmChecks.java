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
package uk.bl.dpt.qa.flint.checks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.wrappers.PDFBoxWrapper;
import uk.bl.dpt.qa.flint.wrappers.iTextWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * Wrapper around additional specific DRM checks that produces an error message
 * in case of a timing out after PDFFormat#Wrapper_TIMEOUT seconds.
 */
public class SpecificDrmChecks extends TimedTask {

    private Logger logger;
    private Set<String> patternFilter;

    /**
     * Create a SpeficDRMChecks Object that times out if calls take longer than expected
     * @param pTimeout timeout to use
     * @param pPatternFilter
     */
    public SpecificDrmChecks(long pTimeout, Set<String> pPatternFilter) {
        super(FixedCategories.NO_DRM.toString(), pTimeout);
        this.patternFilter = pPatternFilter;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public LinkedHashMap<String, CheckCategory> call() throws Exception {
        LinkedHashMap<String, CheckCategory> cmap = new LinkedHashMap<String, CheckCategory>();
        if (patternFilter == null || patternFilter.contains(FixedCategories.NO_DRM.toString()) ) {
            logger.info("Adding specific DRM checks for {} to check-result", contentFile);
            CheckCategory cc = new CheckCategory(FixedCategories.NO_DRM.toString());
            cc.add(new CheckCheck("checkDRMPDFBoxAbsolute", !PDFBoxWrapper.hasDRM(contentFile), null));
            logger.debug(cc.get("checkDRMPDFBoxAbsolute").toString());
            cc.add(new CheckCheck("checkDRMPDFBoxGranular", !PDFBoxWrapper.hasDRMGranular(contentFile), null));
            logger.debug(cc.get("checkDRMPDFBoxGranular").toString());
            cc.add(new CheckCheck("checkDRMNaiive", !checkDRMNaiive(contentFile), null));
            logger.debug(cc.get("checkDRMNaiive").toString());
            cc.add(new CheckCheck("checkDRM_iText", !iTextWrapper.hasDRM(contentFile), null));
            logger.debug(cc.get("checkDRM_iText").toString());
            cmap.put(cc.getName(), cc);
        }
        return cmap;
    }

    /**
     * Search for /encrypt in file
     * NOTE: this might be found in content but if we're being conservative it might be useful
     * @param pStream input-stream
     * @return true if /encrypt found
     */
    private static boolean checkDRMNaiive(InputStream pStream) {

        Scanner scanner = new Scanner(pStream);

        //just try and find the first occurrence of /encrypt (note that this might actually be in the content)
        //scanner.findWithinHorizon("/[rR][oO][oO][tT]", 0);
        String found = scanner.findWithinHorizon("/[eE][nN][cC][rR][yY][pP][tT]", 0);

        //System.out.println("Scanner found: "+(found!=null?"yes":"no"));

        boolean ret = found!=null && found.length() > 0;

        scanner.close();

        return ret;
    }

    /**
     * Search for /encrypt in file
     * NOTE: this might be found in content but if we're being conservative it might be useful
     * @param contentFile input-file
     * @return true if /encrypt is found
     */
    private boolean checkDRMNaiive(File contentFile) {
        try {
            return checkDRMNaiive(new FileInputStream(contentFile));
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        return false;
    }
}
