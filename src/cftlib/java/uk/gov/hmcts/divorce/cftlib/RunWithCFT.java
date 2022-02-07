package uk.gov.hmcts.divorce.cftlib;

import uk.gov.hmcts.divorce.CaseApiApplication;
import uk.gov.hmcts.rse.ccd.lib.api.LibRunner;

@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class RunWithCFT {

    public static void main(final String[] args) {
        LibRunner.run(CaseApiApplication.class);
    }
}
