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
package uk.bl.dpt.qa.flint.wrappers;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for EpubCheck library
 * @author wpalmer
 *
 */
public class EpubCheckWrapper {

    static Logger LOGGER = LoggerFactory.getLogger(EpubCheckWrapper.class);

    private static Map<String, Report> miniCache = new HashMap<String, Report>();

    private EpubCheckWrapper() {}

    public static StreamSource check(File file) throws IOException {
        Report report = miniCache.get(file.getAbsolutePath());
        File reportFile = null;
        if (report == null) {
            reportFile = File.createTempFile("epubcheck-report", "-for-" + file.getName() + ".xml");
            report = new XmlReportWithMessageIds(reportFile, file.getName(), EpubCheck.version());
            EpubCheck check = new EpubCheck(file, report);
            check.validate();
            report.generate();
            LOGGER.info("Generated EpubCheck report at {}");
            miniCache.put(file.getAbsolutePath(), report);
        }
        return new StreamSource(reportFile);
    }
}
