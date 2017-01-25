package uk.bl.dpt.qa.flint.epub.checks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.qa.flint.checks.CheckCategory;
import uk.bl.dpt.qa.flint.checks.TimedTask;
import uk.bl.dpt.qa.flint.formats.EPUBFormat;
import uk.bl.dpt.qa.flint.formats.PolicyAware;
import uk.bl.dpt.qa.flint.wrappers.EpubCheckWrapper;

import javax.xml.transform.stream.StreamSource;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Wrapper around the policy-validation process that produces an error message
 * in case of a timing out after EPUBFormat#Wrapper_TIMEOUT seconds.
 */
public class PolicyValidation extends TimedTask {

    private Logger logger;
    private Set<String> patternFilter;

    /**
     * Constructor for PolicyValidation.
     *
     * @param timeout the time [s] after which a TimeOutException is thrown and logged as
     *                an 'erroneous' {@link uk.bl.dpt.qa.flint.checks.CheckCategory}
     * @param patternFilter a set of strings that represent patterns to be included
     *                      in following operations.
     */
    public PolicyValidation(long timeout, Set<String> patternFilter) {
        super(FixedCategories.POLICY_VALIDATION.toString(), timeout);
        this.patternFilter = patternFilter;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public LinkedHashMap<String, CheckCategory> call() throws Exception {
        logger.info("Performing a policy validation on {}", contentFile);
        StreamSource outputXml = new EpubCheckWrapper().check(contentFile);
        return PolicyAware.policyValidationResult(outputXml,
                new StreamSource(EPUBFormat.getPolicyStatically()), patternFilter);
    }

}
