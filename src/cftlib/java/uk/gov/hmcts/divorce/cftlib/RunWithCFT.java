package uk.gov.hmcts.divorce.cftlib;

import uk.gov.hmcts.divorce.CaseApiApplication;
import uk.gov.hmcts.rse.ccd.lib.api.LibRunner;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class RunWithCFT {

    public static void main(final String[] args) {
        LibRunner.run(CaseApiApplication.class, Set.of(), Map.of(
            "ccd.s2s-authorised.services.case_user_roles", "nfdiv_case_api"
        ));
    }
}
