package uk.gov.hmcts.divorce.cftlib;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Value("ccd-NFD-${CCD_DEF_NAME:dev}.xlsx")
    String defName;

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
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource("classpath:cftlib-am-role-assignments.json")
            .getInputStream(), Charset.defaultCharset());
        lib.configureRoleAssignments(json);

        var def = Files.readAllBytes(Path.of("build/ccd-config/" + defName));
        lib.importDefinition(def);
    }
}
