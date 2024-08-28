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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationSole.CASEWORKER_REQUEST_FOR_INFORMATION_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerRequestForInformationSoleTest {

    @Mock
    private CaseworkerRequestForInformationHelper helper;

    @InjectMocks
    private CaseworkerRequestForInformationSole caseworkerRequestForInformationSole;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRequestForInformationSole.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REQUEST_FOR_INFORMATION_SOLE);
    }

    @Test
    void shouldClearExistingRequestOnAboutToStart() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().setRequestForInformation(new RequestForInformation());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationName(TEST_OTHER_NAME);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRequestForInformationSole.aboutToStart(caseDetails);
        final RequestForInformation responseRequestForInformation =
            response.getData().getRequestForInformationList().getRequestForInformation();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isNull();
    }

    @Test
    void shouldSuccessfullyCompleteMidEventWhenNoErrorsInEmailAddressValidation() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        when(helper.areEmailsValid(any())).thenReturn(new ArrayList<>());

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformationSole.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldFailMidEventWhenErrorsInEmailAddressValidation() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        when(helper.areEmailsValid(any())).thenReturn(Collections.singletonList("Error Text"));

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformationSole.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList("Error Text"));
    }

    @Test
    void shouldSuccessfullyCompleteAboutToSubmitEvent() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        when(helper.createRequestForInformationAndSendNotifications(any())).thenReturn(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRequestForInformationSole.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(AwaitingDocuments);
    }
}
