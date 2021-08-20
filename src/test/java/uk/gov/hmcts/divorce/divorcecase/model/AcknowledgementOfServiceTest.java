package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

class AcknowledgementOfServiceTest {

    @Test
    void shouldSetNoticeOfProceedings() {

        final Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build();

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();

        acknowledgementOfService.setNoticeOfProceedings(solicitor);

        assertThat(acknowledgementOfService.getDigitalNoticeOfProceedings()).isEqualTo(YES);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isEqualTo(TEST_ORG_NAME);
    }
}