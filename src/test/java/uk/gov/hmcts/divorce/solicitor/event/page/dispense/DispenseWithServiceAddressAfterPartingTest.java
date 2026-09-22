package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class DispenseWithServiceAddressAfterPartingTest {

    @InjectMocks
    private DispenseWithServiceAddressAfterParting page;

    @Test
    void shouldReturnErrorWhenDetailsOfEnquiriesNotProvidedForAddress2() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().setDispenseWithServiceJourneyOptions(
            DispenseWithServiceJourneyOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().getDispenseWithServiceJourneyOptions()
            .setDispensePartnerPastAddress2("Test");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).containsExactly(
            "You need to provide details of enquiries made about the second address you provided");
    }
}
