package uk.gov.hmcts.divorce.solicitor.event.page.bailiff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.solicitor.event.page.bailiff.BailiffServiceRespondentPhoneAgePage.ERROR_FUTURE_DOB;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class BailiffServiceRespondentPhoneAgePageTest {

    @InjectMocks
    private BailiffServiceRespondentPhoneAgePage page;

    @Test
    void shouldReturnErrorWhenPartnersDoBIsInTheFuture() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().setBailiffServiceJourneyOptions(
            BailiffServiceJourneyOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().getBailiffServiceJourneyOptions()
            .setBailiffKnowPartnersDateOfBirth(YES);
        caseData.getApplicant1().getInterimApplicationOptions().getBailiffServiceJourneyOptions()
            .setBailiffPartnersDateOfBirth(LocalDate.now().plusDays(1));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).containsExactly(ERROR_FUTURE_DOB);
    }

    @Test
    void shouldValidateWhenPartnersDoBIsNotInTheFuture() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().setBailiffServiceJourneyOptions(
            BailiffServiceJourneyOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().getBailiffServiceJourneyOptions()
            .setBailiffKnowPartnersDateOfBirth(YES);
        caseData.getApplicant1().getInterimApplicationOptions().getBailiffServiceJourneyOptions()
            .setBailiffPartnersDateOfBirth(LocalDate.now().minusYears(35));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }
}
