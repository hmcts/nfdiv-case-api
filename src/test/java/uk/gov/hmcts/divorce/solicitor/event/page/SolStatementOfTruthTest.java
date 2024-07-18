package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolStatementOfTruthTest {

    private final SolStatementOfTruth page = new SolStatementOfTruth();

    @Test
    void shouldReturnErrorsIfSolicitorServiceIsNotSelected() {
        final CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors())
            .contains("Solicitors cannot select personal service. Select court service or solicitor service before proceeding.");
    }

    @Test
    void shouldNotReturnAnyErrorsIfSolicitorServiceIsSelected() {
        final CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorIfCourtServiceSelectedWhenRespondentIsMarkedAsOverseasSoleApp() {
        final CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant2().setAddressOverseas(YesOrNo.YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors())
            .contains("You cannot select Court Service because the Respondent has an international address. "
                + "Please select Solicitor Service.");
    }

    @Test
    void shouldNotReturnErrorIfCourtServiceSelectedWhenRespondentIsMarkedAsOverseasJointApp() {
        final CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant2().setAddressOverseas(YesOrNo.YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }
}
