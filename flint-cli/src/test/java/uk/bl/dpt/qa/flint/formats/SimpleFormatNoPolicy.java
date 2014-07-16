package uk.bl.dpt.qa.flint.formats;

import uk.bl.dpt.qa.flint.checks.CheckCategory;
import uk.bl.dpt.qa.flint.checks.CheckCheck;
import uk.bl.dpt.qa.flint.checks.CheckResult;

import java.io.File;
import java.io.InputStream;
import java.util.*;


 public class SimpleFormatNoPolicy extends PolicyAware implements Format {

    InputStream policy;

    @Override
    public boolean canCheck(File pFile, String pMimetype) {
        String[] nameArray = pFile.getName().split("\\.");
        return nameArray[nameArray.length-1].equals(getFormatName());
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
        CheckCategory cc = new CheckCategory("testCat");
        cc.add(new CheckCheck("testCheck", true, null));
        CheckResult result = new CheckResult(contentFile.getName(), "SIMPLE_FORMAT", this.getVersion());
        result.add(cc);
        result.setTime((long) 1);
        return result;
    }

    @Override
    public Map<String, Map<String, Set<String>>> getFixedCategories() {
        return new HashMap<String, Map<String, Set<String>>>() {{
            put("ALL_GOOD", new HashMap<String, Set<String>>());
        }};
    }

    @Override
    public Collection<String> getAllCategoryNames() throws Exception {
        return getFixedCategories().keySet();
    }

    @Override
    public String getFormatName() {
        return "SIMPLE_FORMAT";
    }

    @Override
    public String getVersion() {
        return "0.1.2.3.4.5";
    }

    @Override
    public InputStream getPolicy() {
        return this.policy;
    }

    public void setPolicy(InputStream input) {
        this.policy = input;
    }
}
