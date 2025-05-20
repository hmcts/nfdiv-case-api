package uk.gov.hmcts.divorce.document.content;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.divorce.common.config.ConditionalOrderCourtDetailsConfig;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.service.PaymentService;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper;

import java.time.Clock;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.hmcts.divorce.document.content.templatecontent"})
public class UniqueTemplateContentProvisionTestConfiguration {

    @MockitoBean
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private DocmosisCommonContent docmosisCommonContent;

    @MockitoBean
    private CommonContent commonContent;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private GenerateCertificateOfEntitlementHelper generateCertificateOfEntitlementHelper;

    @MockitoBean
    private ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;
}
