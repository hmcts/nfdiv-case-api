package uk.gov.hmcts.divorce.cftlib;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftLibConfig implements CFTLibConfigurer {
  @Override
  public void configure(CFTLib lib) throws Exception {
    for (String p : List.of(
        "DivCaseWorkerUser@AAT.com",
        "TEST_CASE_WORKER_USER@mailinator.com",
        "TEST_SOLICITOR@mailinator.com",
        "divorce_as_caseworker_admin@mailinator.com")) {
      lib.createProfile(p, "DIVORCE", "NO_FAULT_DIVORCE", "Submitted");
    }

    lib.createRoles(
        "caseworker-divorce-courtadmin_beta",
        "caseworker-divorce-superuser",
        "caseworker-divorce-courtadmin-la",
        "caseworker-divorce-courtadmin",
        "caseworker-divorce-solicitor",
        "caseworker-divorce-pcqextractor",
        "caseworker-divorce-systemupdate",
        "caseworker-divorce-bulkscan",
        "caseworker-caa",
        "citizen",
        "caseworker-divorce",
        "caseworker",
        "pui-case-manager",
        "pui-finance-manager",
        "pui-organisation-manager",
        "pui-user-manager"
    );
    var def = Files.readAllBytes(Path.of("build/ccd-config/ccd-NFD-dev.xlsx"));
    lib.importDefinition(def);
  }
}
