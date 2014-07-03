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
import uk.bl.dpt.qa.flint.formats.PDFFormat;
import uk.bl.dpt.qa.flint.formats.PolicyAware;
import uk.bl.dpt.qa.flint.wrappers.PDFBoxWrapper;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;

/**
 * Wrapper around the policy-validation process that produces an error message
 * in case of a timing out after PDFFormat#Wrapper_TIMEOUT seconds.
 */
public class PolicyValidation extends TimedTask  {

    private Logger logger;

    public PolicyValidation(long timeout) {
        super(FixedCategories.POLICY_VALIDATION.toString(), timeout);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public LinkedHashMap<String, CheckCategory> call() throws Exception {
        logger.info("Performing a policy validation on {}", contentFile);
        ByteArrayOutputStream outputXml = PDFBoxWrapper.preflightToXml(contentFile);
        return PolicyAware.policyValidationResult(new StreamSource(new ByteArrayInputStream(outputXml.toByteArray())),
                new StreamSource(PDFFormat.getPolicyStatically()));
    }

}