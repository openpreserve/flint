package uk.bl.dpt.qa.flint;


import java.io.File;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import uk.bl.dpt.qa.flint.checks.CheckResult;


public class FlintMobiTest {

    private Flint flint;
    
    private static String DRM_MOBI_CHECK = "DRM check";
    
    @Before
    public void setUp() throws Exception {
        flint = new Flint();
    }
    
    @Test
    public final void testMobiNoDRM() {
        File toTest = new File(FlintMobiTest.class.getResource("/mobisamples/lorem-ipsum.mobi").getPath());
        CheckResult result = flint.check(toTest).get(0);
        assertTrue("DRM should not be found", result.get(DRM_MOBI_CHECK).isHappy());
        assertTrue("DRM should not be found", result.get(DRM_MOBI_CHECK).get("checkForEncryption").isHappy());
        
        toTest = new File(FlintMobiTest.class.getResource("/mobisamples/lorem-ipsum.azw3").getPath());
        result = flint.check(toTest).get(0);
        assertTrue("DRM should not be found", result.get(DRM_MOBI_CHECK).isHappy());
        assertTrue("DRM should not be found", result.get(DRM_MOBI_CHECK).get("checkForEncryption").isHappy());

    }

}
