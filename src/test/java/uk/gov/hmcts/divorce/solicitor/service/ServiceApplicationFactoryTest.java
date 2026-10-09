package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationFactoryTest {

    @InjectMocks
    private ServiceApplicationFactory serviceApplicationFactory;

    @Mock
    private Clock clock;

    @Test
    void shouldCreateServiceApplicationWithHelpWithFeesPaymentWhenDocumentsAreProvided() {
        setMockClock(clock);

        List<ListValue<DivorceDocument>> evidenceDocs = List.of(
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder().build())
                .build()
        );

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
            .interimAppsEvidenceDocs(evidenceDocs)
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEES_HELP_WITH)
            .build();

        AlternativeService serviceApplication = serviceApplicationFactory.createFromInterimOptions(options);

        assertThat(serviceApplication.getReceivedServiceApplicationDate()).isEqualTo(getExpectedLocalDate());
        assertThat(serviceApplication.getReceivedServiceAddedDate()).isEqualTo(getExpectedLocalDate());
        assertThat(serviceApplication.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);
        assertThat(serviceApplication.getServiceApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(serviceApplication.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.YES);
        assertThat(serviceApplication.getServiceApplicationDocuments()).isEqualTo(evidenceDocs);
        assertThat(serviceApplication.getAlternativeServiceFeeRequired()).isEqualTo(YesOrNo.YES);
        assertThat(serviceApplication.getServicePaymentFee().getPaymentMethod()).isEqualTo(ServicePaymentMethod.FEE_PAY_BY_HWF);
    }

    @Test
    void shouldCreateServiceApplicationWithAccountPaymentWhenNoDocumentsAreProvided() {
        setMockClock(clock);

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
            .interimAppsPaymentMethod(SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT)
            .build();

        AlternativeService serviceApplication = serviceApplicationFactory.createFromInterimOptions(options);

        assertThat(serviceApplication.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.NO);
        assertThat(serviceApplication.getServiceApplicationDocuments()).isNull();
        assertThat(serviceApplication.getServicePaymentFee().getPaymentMethod()).isEqualTo(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
    }
}
