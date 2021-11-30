package uk.gov.hmcts.divorce.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class Applicant2SolReviewApplicant1ApplicationTest {

    private final Applicant2SolReviewApplicant1Application page = new Applicant2SolReviewApplicant1Application();

    @Test
    public void shouldPreventProgressIfRespondentHasNotReadPetition() {
        final CaseData caseData = caseData();
        caseData.getAcknowledgementOfService().setConfirmReadPetition(NO);
        caseData.getLabelContent().setTheApplicant2("the respondent");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).containsExactly(
            "To continue,the respondent must have read the application in order to respond"
        );
    }

    @Test
    public void shouldAllowProgressIfRespondentHasReadPetition() {
        final CaseData caseData = caseData();
        caseData.getAcknowledgementOfService().setConfirmReadPetition(YES);
        caseData.getLabelContent().setTheApplicant2("respondent");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }
}
