package uk.bl.dpt.qa.flint.mobi.checks;


/**
 * There are two types of Check Categories:
 * (1) dynamically defined ones from within a (possibly changing) schematron
 *     policy (aka schematron patterns)
 * (2) additional static ones that require specific check methods and are added
 *     'manually' to the dynamic list; these ones are the FixedCategories.
 */
public enum FixedCategories {

    WELL_FORMED("Well formed"),
    NO_DRM_ENCRYPTION("DRM check"),
    POLICY_VALIDATION("Overall error indicator for policy validation");
    
    private final String cat;

    FixedCategories(String cat) {
        this.cat = cat;
    }

    public String toString() {
        return cat;
    }
    
}
