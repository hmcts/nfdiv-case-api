package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerApplicantResponded.CASEWORKER_APPLICANT_RESPONDED;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerApplicantResponded.ERROR_ALREADY_ISSUED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2WithAddress;

@ExtendWith(MockitoExtension.class)
class CaseworkerApplicantRespondedTest {

    @InjectMocks
    private CaseworkerApplicantResponded caseworkerApplicantResponded;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerApplicantResponded.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_APPLICANT_RESPONDED);
    }

    @Test
    void shouldThrowIfCaseAlreadyIssued() {
        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(LocalDate.now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(State.AwaitingDocuments);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerApplicantResponded.aboutToStart(caseDetails);

        assertThat(response.getErrors()).contains(ERROR_ALREADY_ISSUED);
    }

    @Test
    void shouldNotThrowIfCaseNotIssued() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData());

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerApplicantResponded.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotChangeStateIfRespondentAddressNotProvided() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(State.AwaitingDocuments);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerApplicantResponded.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
    }

    @Test
    void shouldSetStateToSubmittedIfRespondentAddressProvided() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant2WithAddress());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(State.AwaitingDocuments);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerApplicantResponded.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(State.Submitted);
    }
}
