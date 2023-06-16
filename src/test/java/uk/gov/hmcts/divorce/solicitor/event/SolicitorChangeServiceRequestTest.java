package uk.gov.hmcts.divorce.solicitor.event;

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

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorChangeServiceRequest.SOLICITOR_CHANGE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;

@ExtendWith(MockitoExtension.class)
class SolicitorChangeServiceRequestTest {

    @InjectMocks
    private SolicitorChangeServiceRequest solicitorChangeServiceRequest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorChangeServiceRequest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_CHANGE_SERVICE_REQUEST);
    }

    @Test
    void shouldThrowErrorIfPersonalService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Personal Service. Please select Solicitor or Court Service.");
    }
}
