package uk.gov.hmcts.divorce;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
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
@Slf4j
public class CaseApiApplication implements CommandLineRunner {

    public static void main(final String[] args) {
        SpringApplication application = new SpringApplication(CaseApiApplication.class);

        if (args.length == 0) {
            application.run(args);
        } else {
            application.setWebApplicationType(WebApplicationType.NONE);
            application.run(args).close();
        }
    }

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            return;
        }

        log.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            log.info("args[{}]: {}", i, args[i]);
        }
    }
}
