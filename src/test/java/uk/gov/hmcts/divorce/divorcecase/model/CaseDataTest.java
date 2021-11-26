package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;

class CaseDataTest {

    @Test
    void shouldReturnApplicant2EmailIfApplicant2EmailIsSet() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsNullAndApplicant2InviteEmailAddressIsSet() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email("")
                .build())
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnApplicant2InviteEmailIfApplicant2EmailIsBlankAndApplicant2InviteEmailAddressIsSet() {

        final CaseData caseData = CaseData.builder()
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL)
                .build())
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isEqualTo(TEST_APPLICANT_2_USER_EMAIL);
    }

    @Test
    void shouldReturnNullIfApplicant2EmailIsNullAndCaseInviteIsNull() {

        final CaseData caseData = CaseData.builder()
            .build();

        assertThat(caseData.getApplicant2EmailAddress()).isNull();
    }
}
