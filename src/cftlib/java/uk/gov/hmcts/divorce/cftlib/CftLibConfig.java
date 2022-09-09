package uk.gov.hmcts.divorce.cftlib;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CCDDefinitionGenerator;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Autowired
    CCDDefinitionGenerator configWriter;

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
            "payments",
            "pui-case-manager",
            "pui-finance-manager",
            "pui-organisation-manager",
            "pui-user-manager"
        );
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource("classpath:cftlib-am-role-assignments.json")
            .getInputStream(), Charset.defaultCharset());
        lib.configureRoleAssignments(json);


        // Generate CCD definitions
        configWriter.generateAllCaseTypesToJSON(new File("build/definitions"));

        // Import CCD definitions
        lib.importJsonDefinition(new File("build/definitions/NFD"));
        lib.importJsonDefinition(new File("build/definitions/NO_FAULT_DIVORCE_BulkAction"));
    }
}
