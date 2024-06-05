package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAmendCase.CASEWORKER_AMEND_CASE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.LESS_THAN_ONE_YEAR_SINCE_SUBMISSION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithMarriageDate;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class CaseworkerAmendCaseTest {

    @Mock
    private GenerateApplication generateApplication;

    @InjectMocks
    private CaseworkerAmendCase caseworkerAmendCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAmendCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_AMEND_CASE);
    }

    @Test
    void shouldReturnErrorIfMarriageDateLessThanOneYearPriorToApplicationSubmittedDate() {
        final CaseData caseData = caseDataWithMarriageDate();
        caseData.getApplication().setDateSubmitted(LocalDateTime.now());
        caseData.getApplication().getMarriageDetails().setDate(LocalDate.from(caseData.getApplication().getDateSubmitted().minusDays(1)));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(State.Submitted);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEqualTo(List.of("MarriageDate" + LESS_THAN_ONE_YEAR_SINCE_SUBMISSION));
    }

    @Test
    void shouldNotReturnErrorIfMarriageDateMoreThanOneYearPriorToApplicationSubmittedDate() {
        final CaseData caseData = caseDataWithMarriageDate();
        caseData.getApplication().setDateSubmitted(LocalDateTime.now());
        caseData.getApplication().getMarriageDetails().setDate(
            LocalDate.from(caseData.getApplication().getDateSubmitted().minusYears(1).minusDays(1))
        );
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(State.Submitted);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getWarnings()).isNull();
    }

    @Test
    void shouldGenerateD8IfApplicationPreviouslyIssued() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(State.AwaitingAos);

        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);

        caseworkerAmendCase.aboutToSubmit(caseDetails, caseDetails);

        verify(generateApplication).apply(caseDetails);
    }

    @Test
    void shouldNotGenerateD8IfApplicationNotYetIssued() {
        final CaseData caseData = validCaseDataForIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(State.AwaitingAos);

        caseworkerAmendCase.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(generateApplication);
    }
}
