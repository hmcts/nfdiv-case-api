package uk.gov.hmcts.reform.divorce.caseapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.divorce.caseapi.clients.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@SpringBootApplication
@EnableFeignClients(
    clients = {
        IdamApi.class,
        ServiceAuthorisationApi.class,
        CaseUserApi.class,
        FeesAndPaymentsClient.class
    }
)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class CaseApiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CaseApiApplication.class, args);
    }
}
