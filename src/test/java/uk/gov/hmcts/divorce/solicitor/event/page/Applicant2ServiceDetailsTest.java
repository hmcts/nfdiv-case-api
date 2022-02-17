package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class Applicant2ServiceDetailsTest {

    private static final String EMAIL = "test@email.com";

    private final Applicant2ServiceDetails page = new Applicant2ServiceDetails();

    @Test
    void shouldPreventProgressIfBothEmailAndHomeAddressNotProvided() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getLabelContent().setRespondentsOrApplicant2s("respondent's");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).containsExactly(
            "You must provide respondent's email or a postal address"
        );
    }

    @Test
    void shouldProgressIfEitherEmailOrHomeAddressProvided() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getLabelContent().setRespondentsOrApplicant2s("respondent's");
        caseData.getApplicant2().setEmail(EMAIL);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors().size()).isEqualTo(0);
    }

    @Test
    void shouldNotFireValidationIfApplicationTypeIsJoint() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getLabelContent().setRespondentsOrApplicant2s("applicant 2's");
        caseData.getApplicant2().setEmail(EMAIL);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors().size()).isEqualTo(0);
    }
}
