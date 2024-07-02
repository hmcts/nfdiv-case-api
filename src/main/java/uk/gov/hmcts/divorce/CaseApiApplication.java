package uk.gov.hmcts.divorce;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.divorce.document.DocAssemblyClient;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsClient;
import uk.gov.hmcts.divorce.payment.PaymentClient;
import uk.gov.hmcts.divorce.payment.PaymentPbaClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaRefDataClient;
import uk.gov.hmcts.divorce.systemupdate.service.ScheduledTaskRunner;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.util.TimeZone;

@SpringBootApplication(
    scanBasePackages = {"uk.gov.hmcts.ccd.sdk", "uk.gov.hmcts.divorce", "uk.gov.hmcts.reform.idam.client",
        "uk.gov.hmcts.reform.sendletter", "uk.gov.hmcts.reform.ccd.document.am.feign"}
)
@EnableFeignClients(
    clients = {
        AssignCaseAccessClient.class,
        IdamApi.class,
        ServiceAuthorisationApi.class,
        CaseUserApi.class,
        FeesAndPaymentsClient.class,
        DocAssemblyClient.class,
        CoreCaseDataApi.class,
        CaseAssignmentApi.class,
        CaseDocumentClientApi.class,
        OrganisationClient.class,
        PbaRefDataClient.class,
        PaymentPbaClient.class,
        CaseEventsApi.class,
        PaymentClient.class
    }
)
@EnableScheduling
@EnableRetry
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@Slf4j
public class CaseApiApplication implements CommandLineRunner {

    @Autowired
    ScheduledTaskRunner taskRunner;

    public static void main(final String[] args) {
        final var application = new SpringApplication(CaseApiApplication.class);
        final var instance = application.run(args);

        if (System.getenv("TASK_NAME") != null) {
            instance.close();
        }
    }

    @Override
    public void run(String... args) {
        if (System.getenv("TASK_NAME") != null) {
            taskRunner.run(System.getenv("TASK_NAME"));
        }
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
    }
}
