package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralPartiesTest {

    @Test
    void shouldReturnCorrectPartyNameForSoleApplicant() {
        assertThat(GeneralParties.from(true, ApplicationType.SOLE_APPLICATION))
            .isEqualTo(GeneralParties.APPLICANT);
    }

    @Test
    void shouldReturnCorrectPartyNameForJointApplicant() {
        assertThat(GeneralParties.from(true, ApplicationType.JOINT_APPLICATION))
            .isEqualTo(GeneralParties.APPLICANT);
    }

    @Test
    void shouldReturnCorrectPartyNameForSoleRespondent() {
        assertThat(GeneralParties.from(false, ApplicationType.SOLE_APPLICATION))
            .isEqualTo(GeneralParties.RESPONDENT);
    }

    @Test
    void shouldReturnCorrectPartyNameForJointApplicant2() {
        assertThat(GeneralParties.from(false, ApplicationType.JOINT_APPLICATION))
            .isEqualTo(GeneralParties.APPLICANT2);
    }
}
