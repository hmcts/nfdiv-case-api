package uk.gov.hmcts.divorce.cftlib;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CCDDefinitionGenerator;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Autowired
    CCDDefinitionGenerator configWriter;

    @Override
    public void configure(CFTLib lib) throws Exception {
        var users = Map.of(
            "DivCaseWorkerUser@AAT.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-courtadmin_beta"),
            "TEST_CASE_WORKER_USER@mailinator.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-courtadmin_beta"),
            "TEST_SOLICITOR@mailinator.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-solicitor"),
            "TEST_JUDGE@mailinator.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-judge"),
            "dummysystemupdate@test.com", List.of("caseworker", "caseworker-divorce", "caseworker-divorce-systemupdate"),
            "role.assignment.admin@gmail.com", List.of("caseworker"),
            "data.store.idam.system.user@gmail.com", List.of("caseworker"),
            "divorce_as_caseworker_admin@mailinator.com", List.of("caseworker-divorce", "caseworker-divorce-superuser"));

        for (var entry : users.entrySet()) {
            lib.createIdamUser(entry.getKey(), entry.getValue().toArray(new String[0]));
            lib.createProfile(entry.getKey(), "DIVORCE", "NO_FAULT_DIVORCE", "Submitted");
        }

        lib.createRoles(
            "caseworker-divorce-courtadmin_beta",
            "caseworker-divorce-superuser",
            "caseworker-divorce-courtadmin-la",
            "caseworker-divorce-courtadmin",
            "caseworker-divorce-solicitor",
            "caseworker-divorce-judge",
            "caseworker-divorce-pcqextractor",
            "caseworker-divorce-systemupdate",
            "caseworker-divorce-bulkscan",
            "caseworker-caa",
            "caseworker-approver",
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

        File source = new File("ccd-definitions");
        File dest = new File("build/definitions/NFD");
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Import CCD definitions
        lib.importJsonDefinition(new File("build/definitions/NFD"));
        lib.importJsonDefinition(new File("build/definitions/NO_FAULT_DIVORCE_BulkAction"));
    }
}
