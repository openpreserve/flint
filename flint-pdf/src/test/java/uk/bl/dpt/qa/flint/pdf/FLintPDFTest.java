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
package uk.bl.dpt.qa.flint.pdf;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.bl.dpt.qa.flint.Flint;
import uk.bl.dpt.qa.flint.checks.CheckResult;
import uk.bl.dpt.qa.flint.pdf.checks.FixedCategories;

@SuppressWarnings("javadoc")
public class FlintPDFTest {

    private Flint drmlint;

    private static String DRM = FixedCategories.NO_DRM.toString();

    @Before
    public void setUp() throws Exception {
        drmlint = new Flint();
    }

    // pdfPolicyValidation tests on the same files as previous tests
    @Test
    public void testFormatCorpusEncryptionOpenPassword() {
        File toTest = new File(FlintPDFTest.class.getResource("/format_corpus/encryption_openpassword.pdf").getPath());
        CheckResult result = drmlint.check(toTest).get(0);
        Assert.assertFalse("drm not found when it should be", result.get(DRM).isHappy());
        Assert.assertFalse(result.get("Checks for encryption").get("Open password").isHappy());
        Assert.assertEquals("1", result.get("Checks for encryption").get("Open password").getErrorCount().toString());

    }

    @Test
    public void testPolicyFormatCorpusEncryptionNoTextAccess() {
        File toTest = new File(FlintPDFTest.class.getResource("/format_corpus/encryption_notextaccess.pdf").getPath());
        CheckResult result = drmlint.check(toTest).get(0);
        Assert.assertFalse("drm not found when it should be", result.get(DRM).isHappy());
        Assert.assertFalse(result.get("Checks for encryption").get("Encryption").isHappy());
        Assert.assertEquals("1", result.get("Checks for encryption").get("Encryption").getErrorCount().toString());
    }

    @Test
    public final void testPolicyFormatCorpusEncryptionNoPrinting() {
        File toTest = new File(FlintPDFTest.class.getResource("/format_corpus/encryption_noprinting.pdf").getPath());
        CheckResult result = drmlint.check(toTest).get(0);
        Assert.assertFalse("drm not found when it should be", result.get(DRM).isHappy());
        Assert.assertFalse(result.get("Checks for encryption").get("Encryption").isHappy());
        Assert.assertEquals("1", result.get("Checks for encryption").get("Encryption").getErrorCount().toString());
    }

    @Test
    public final void testPolicyFormatCorpusEncryptionNoCopy() {
        File toTest = new File(FlintPDFTest.class.getResource("/format_corpus/encryption_nocopy.pdf").getPath());
        CheckResult result = drmlint.check(toTest).get(0);
        Assert.assertFalse("drm not found when it should be", result.get(DRM).isHappy());
        Assert.assertFalse(result.get("Checks for encryption").isHappy());
        Assert.assertFalse(result.get("Checks for encryption").get("Encryption").isHappy());
        Assert.assertEquals("1", result.get("Checks for encryption").get("Encryption").getErrorCount().toString());
    }

    @Test
    public void testPolicyFormatCorpusTextOnlyFontsEmbeddedAll() {
        File toTest = new File(FlintPDFTest.class.getResource("/format_corpus/text_only_fontsEmbeddedAll.pdf").getPath());
        CheckResult result = drmlint.check(toTest).get(0);
        Assert.assertTrue("drm found when it should NOT be", result.get(DRM).isHappy());
        Assert.assertTrue(result.isHappy());
    }

}
