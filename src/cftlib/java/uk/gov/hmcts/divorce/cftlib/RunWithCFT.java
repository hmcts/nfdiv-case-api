package uk.gov.hmcts.divorce.cftlib;

import java.util.Map;
import uk.gov.hmcts.divorce.CaseApiApplication;
import uk.gov.hmcts.rse.ccd.lib.api.LibRunner;

public class RunWithCFT {

    public static void main(final String[] args) {
      new LibRunner(CaseApiApplication.class).run(Map.of(
         "management.health.case-document-am-api.enabled", "false"
      ));
    }
}
