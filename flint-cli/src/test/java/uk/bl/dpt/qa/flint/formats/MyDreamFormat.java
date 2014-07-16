package uk.bl.dpt.qa.flint.formats;

import uk.bl.dpt.qa.flint.checks.CheckResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ageuder on 14/05/2014.
 */
@SuppressWarnings("unused")
public class MyDreamFormat extends PolicyAware implements Format {
    @Override
    public boolean canCheck(File pFile, String pMimetype) {
            return pMimetype.split("/")[1].equals(getFormatName());
    }

    @Override
    public boolean canCheck(String pMimetype) {
        return false;
    }

    @Override
    public Collection<String> acceptedMimeTypes() {
        return null;
    }

    @Override
    public CheckResult validationResult(File contentFile) {
            return null;
    }
    @Override
    public Map<String, Map<String, Set<String>>> getFixedCategories() {
        return new HashMap<String, Map<String, Set<String>>>();
    }
    @Override
    public Collection<String> getAllCategoryNames() throws Exception { return null; }
    @Override
    public String getFormatName() {
            return "MY_DREAM";
    }
    @Override
    public String getVersion() {
            return "123";
    }
    @Override
    public InputStream getPolicy() {
        return getClass().getResourceAsStream("/test-policy-file");
    }

}
