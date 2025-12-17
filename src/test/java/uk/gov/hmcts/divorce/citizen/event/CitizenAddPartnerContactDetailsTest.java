package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.citizen.event.CitizenAddPartnerContactDetails.CITIZEN_ADD_PARTNER_CONTACT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
@ExtendWith(SpringExtension.class)
class CitizenAddPartnerContactDetailsTest {

    @InjectMocks
    private CitizenAddPartnerContactDetails citizenAddPartnerContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenAddPartnerContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_ADD_PARTNER_CONTACT);
    }

    @Test
    void shouldNotTransitionStateWhenStateIsNotAwaitingDocuments() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setState(State.AwaitingHWFDecision);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingHWFDecision);
    }

    @Test
    void shouldNotTransitionStateWhenStateIsAwaitingDocumentsButApplicantCouldNotProvideDocuments() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setState(State.AwaitingDocuments);

        var caseData = CaseData.builder().build();
        caseData.getApplication().setApplicant1CannotUpload(YesOrNo.YES);

        caseDetails.setData(caseData);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
    }

    @Test
    void shouldTransitionToSubmittedState() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setState(State.AwaitingDocuments);

        var caseData = CaseData.builder().build();
        caseData.getApplication().setApplicant1CannotUpload(YesOrNo.NO);

        caseDetails.setData(caseData);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.Submitted);
    }
}
