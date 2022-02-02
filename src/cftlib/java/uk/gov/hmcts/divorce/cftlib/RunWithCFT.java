package uk.gov.hmcts.divorce.cftlib;

import uk.gov.hmcts.divorce.CaseApiApplication;
import uk.gov.hmcts.rse.ccd.lib.api.LibRunner;

public class RunWithCFT {

    public static void main(final String[] args) {
      new LibRunner(CaseApiApplication.class).run();
    }
}
