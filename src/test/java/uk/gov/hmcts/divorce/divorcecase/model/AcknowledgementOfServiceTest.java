package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

class AcknowledgementOfServiceTest {

    @Test
    void shouldSetNoticeOfProceedingsToSolicitorIfApplicantIsRepresented() {

        final Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build();
        final Applicant applicant = Applicant.builder()
            .solicitor(solicitor)
            .solicitorRepresented(YES)
            .build();

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();

        acknowledgementOfService.setNoticeOfProceedings(applicant);

        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isEqualTo(TEST_ORG_NAME);
    }

    @Test
    void shouldSetNoticeOfProceedingsToSolicitorWithNoOrganisationPolicy() {

        final Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .firmName(TEST_ORG_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .build();
        final Applicant applicant = Applicant.builder()
            .solicitor(solicitor)
            .solicitorRepresented(YES)
            .build();

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();

        acknowledgementOfService.setNoticeOfProceedings(applicant);

        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isEqualTo(TEST_ORG_NAME);
    }

    @Test
    void shouldSetNoticeOfProceedingsToSolicitorFirmNameWithNoOrganisationPolicyOrOrganisation() {

        final Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .firmName(TEST_ORG_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(OrganisationPolicy.<UserRole>builder().build())
            .build();
        final Applicant applicant = Applicant.builder()
            .solicitor(solicitor)
            .solicitorRepresented(YES)
            .build();

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();

        acknowledgementOfService.setNoticeOfProceedings(applicant);

        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_SOLICITOR_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isEqualTo(TEST_ORG_NAME);
    }

    @Test
    void shouldSetNoticeOfProceedingsEmailToApplicantIfApplicantNotIsRepresented() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .email(TEST_USER_EMAIL)
            .build();

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder().build();

        acknowledgementOfService.setNoticeOfProceedings(applicant);

        assertThat(acknowledgementOfService.getNoticeOfProceedingsEmail()).isEqualTo(TEST_USER_EMAIL);
        assertThat(acknowledgementOfService.getNoticeOfProceedingsSolicitorFirm()).isNullOrEmpty();
    }
}
