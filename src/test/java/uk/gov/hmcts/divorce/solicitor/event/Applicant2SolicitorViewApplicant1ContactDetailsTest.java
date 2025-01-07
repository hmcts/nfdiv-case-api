package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorViewApplicant1ContactDetails.APPLICANT_2_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorViewApplicant1ContactDetails.CONFIDENTIAL_APPLICANT_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class Applicant2SolicitorViewApplicant1ContactDetailsTest {

    @InjectMocks
    private Applicant2SolicitorViewApplicant1ContactDetails solicitorViewApplicant1ContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorViewApplicant1ContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO);
    }

    @Test
    public void aboutToStartShouldReturnValidationErrorIfApplicant1IsPrivate() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PUBLIC).build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorViewApplicant1ContactDetails.aboutToStart(details);

        assertThat(response.getErrors()).containsExactly(CONFIDENTIAL_APPLICANT_ERROR);
    }

    @Test
    public void aboutToStartShouldNotReturnValidationErrorIfApplicant1IsPublic() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PUBLIC).build())
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorViewApplicant1ContactDetails.aboutToStart(details);

        assertThat(response.getErrors()).isEmpty();
    }
}
