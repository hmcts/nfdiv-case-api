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
import uk.gov.hmcts.divorce.caseworker.service.ReIssueApplicationService;
import uk.gov.hmcts.divorce.common.exception.InvalidDataException;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_VALIDATION_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForReIssueApplication;

@ExtendWith(MockitoExtension.class)
class CaseworkerReissueApplicationTest {

    @Mock
    private ReIssueApplicationService reIssueApplicationService;

    @InjectMocks
    private CaseworkerReissueApplication caseworkerReissue;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerReissue.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REISSUE_APPLICATION);
    }

    @Test
    void shouldReturnErrorForAboutToSubmitIfInvalidReissueOptionExceptionIsThrown() {

        final CaseData caseData = validCaseDataForReIssueApplication();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        doThrow(new InvalidReissueOptionException("")).when(reIssueApplicationService).process(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Invalid reissue option, browser page refresh may have occurred. "
            + "Please use 'Previous' button and select a reissue option");
    }

    @Test
    void shouldReturnErrorIfCaseNotIssued() {

        final CaseData caseData = validCaseDataForIssueApplication();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReissue.aboutToStart(caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0))
            .isEqualTo("Case has not been issued therefore it cannot be reissued");
    }

    @Test
    void shouldReturnErrorIfAosHasAlreadyBeenSubmitted() {

        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getAcknowledgementOfService().setDateAosSubmitted(LocalDateTime.of(2021, 10, 26, 10, 0, 0));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReissue.aboutToStart(caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0))
            .isEqualTo("Acknowledgement of Service has been submitted therefore this case cannot be reissued");
    }

    @Test
    void shouldNotReturnErrorIfAosIsYetToBeSubmitted() {

        final CaseData caseData = validCaseDataForReIssueApplication();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReissue.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnValidationErrorsWhenThereIsAnInvalidDataException() {
        final var caseData = invalidCaseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        when(reIssueApplicationService.process(details))
            .thenThrow(new InvalidDataException(
                "dummy details",
                null,
                Collections.singletonList(TEST_VALIDATION_ERROR)
            ));

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReissue.aboutToSubmit(details, null);

        assertThat(response.getErrors())
            .contains(TEST_VALIDATION_ERROR);
    }
}
