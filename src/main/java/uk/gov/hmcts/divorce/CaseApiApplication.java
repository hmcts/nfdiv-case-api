package uk.gov.hmcts.divorce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.divorce.document.DocAssemblyClient;
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataClientAutoConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@SpringBootApplication(
    exclude = {CoreCaseDataClientAutoConfiguration.class},
    scanBasePackages = {"uk.gov.hmcts.ccd.sdk", "uk.gov.hmcts.divorce"}
)
@EnableFeignClients(
    clients = {
        IdamApi.class,
        ServiceAuthorisationApi.class,
        CaseUserApi.class,
        FeesAndPaymentsClient.class,
        DocAssemblyClient.class,
        CoreCaseDataApi.class,
        DocumentManagementClient.class,
        OrganisationClient.class
    }
)
@EnableScheduling
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CaseApiApplication.class, args);
    }
}
