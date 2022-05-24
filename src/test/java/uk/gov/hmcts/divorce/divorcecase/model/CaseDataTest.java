package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
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

    @Test
    void shouldReturnTrueIfCaseDataIsDivorce() {

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();

        assertThat(caseData.isDivorce()).isTrue();
    }

    @Test
    void shouldReturnFalseIfCaseDataIsDissolution() {

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .build();

        assertThat(caseData.isDivorce()).isFalse();
    }

    @Test
    void shouldReturnFirstAlternativeServiceOutcomeIfPresent() {

        final CaseData caseData = CaseData.builder().build();

        assertThat(caseData.getFirstAlternativeServiceOutcome()).isEqualTo(Optional.empty());

        final AlternativeServiceOutcome alternativeServiceOutcome1 = AlternativeService
            .builder()
            .alternativeServiceType(DEEMED)
            .build()
            .getOutcome();
        final AlternativeServiceOutcome alternativeServiceOutcome2 = AlternativeService
            .builder()
            .alternativeServiceType(DISPENSED)
            .build()
            .getOutcome();

        final List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes = new ArrayList<>();
        alternativeServiceOutcomes.add(0, ListValue.<AlternativeServiceOutcome>builder()
            .value(alternativeServiceOutcome2)
            .build());
        alternativeServiceOutcomes.add(1, ListValue.<AlternativeServiceOutcome>builder()
            .value(alternativeServiceOutcome1)
            .build());

        caseData.setAlternativeServiceOutcomes(alternativeServiceOutcomes);

        assertThat(caseData.getFirstAlternativeServiceOutcome()).isEqualTo(Optional.of(alternativeServiceOutcome2));
    }
}
