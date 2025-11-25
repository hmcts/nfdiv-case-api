package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


class AlternativeServiceTest {

    private static final String BAILIFF_EMAIL = "email";
    private static final String BAILIFF_COURT = "court";
    private static final LocalDate APPLICATION_DATE = LocalDate.of(2020, 1, 1);

    @Test
    void shouldBuildAlternativeServiceOutcome() {
        final DivorceDocument serviceApplicationAnswers = DivorceDocument.builder().build();
        final Bailiff bailiff = Bailiff.builder()
            .localCourtEmail(BAILIFF_EMAIL)
            .localCourtName(BAILIFF_COURT)
            .certificateOfServiceDocument(serviceApplicationAnswers)
            .successfulServedByBailiff(YesOrNo.YES)
            .build();

        final AlternativeService alternativeService = AlternativeService.builder()
            .alternativeServiceType(AlternativeServiceType.BAILIFF)
            .receivedServiceApplicationDate(APPLICATION_DATE)
            .receivedServiceAddedDate(APPLICATION_DATE)
            .serviceApplicationAnswers(serviceApplicationAnswers)
            .serviceApplicationSubmittedOnline(YesOrNo.YES)
            .serviceApplicationDocsUploadedPreSubmission(YesOrNo.YES)
            .serviceApplicationGranted(YesOrNo.YES)
            .refusalReason(ServiceApplicationRefusalReason.ADMIN_REFUSAL)
            .serviceApplicationDecisionDate(APPLICATION_DATE)
            .deemedServiceDate(APPLICATION_DATE)
            .bailiff(bailiff).build();

        final AlternativeServiceOutcome result = alternativeService.getOutcome();

        assertThat(result.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.BAILIFF);
        assertThat(result.getReceivedServiceApplicationDate()).isEqualTo(APPLICATION_DATE);
        assertThat(result.getReceivedServiceAddedDate()).isEqualTo(APPLICATION_DATE);
        assertThat(result.getServiceApplicationAnswers()).isEqualTo(serviceApplicationAnswers);
        assertThat(result.getServiceApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(result.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.YES);
        assertThat(result.getServiceApplicationGranted()).isEqualTo(YesOrNo.YES);
        assertThat(result.getRefusalReason()).isEqualTo(ServiceApplicationRefusalReason.ADMIN_REFUSAL);
        assertThat(result.getServiceApplicationDecisionDate()).isEqualTo(APPLICATION_DATE);
        assertThat(result.getDeemedServiceDate()).isEqualTo(APPLICATION_DATE);
        assertThat(result.getLocalCourtEmail()).isEqualTo(BAILIFF_EMAIL);
        assertThat(result.getLocalCourtName()).isEqualTo(BAILIFF_COURT);
    }
}
