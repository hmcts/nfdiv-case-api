package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class MarriageIrretrievablyBrokenForApplicant2Test {

    @Mock
    private MarriageIrretrievablyBroken marriageIrretrievablyBrokenPage;

    @InjectMocks
    private MarriageIrretrievablyBrokenForApplicant2 marriageIrretrievablyBrokenForApplicant2Page;

    @Test
    public void shouldPreventProgressIfMarriageNotBroken() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            AboutToStartOrSubmitResponse
                .<CaseData, State>builder()
                .errors(singletonList("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken"))
                .build();

        when(marriageIrretrievablyBrokenPage.midEvent(details, details)).thenReturn(response);

        AboutToStartOrSubmitResponse<CaseData, State> actualResponse =
            marriageIrretrievablyBrokenForApplicant2Page.midEvent(details, details);

        assertThat(actualResponse.getErrors().size()).isOne();
        assertThat(actualResponse.getErrors())
            .containsExactly("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken");
    }

    @Test
    public void shouldPreventProgressIfMarriageNotBrokenForBothApplicants() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(NO);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            AboutToStartOrSubmitResponse
                .<CaseData, State>builder()
                .errors(List.of(
                    "To continue, applicant 1 must believe and declare that their marriage has irrevocably broken",
                    "To continue, applicant 2 must believe and declare that their marriage has irrevocably broken")
                )
                .build();

        when(marriageIrretrievablyBrokenPage.midEvent(details, details)).thenReturn(response);

        AboutToStartOrSubmitResponse<CaseData, State> actualResponse =
            marriageIrretrievablyBrokenForApplicant2Page.midEvent(details, details);

        assertThat(actualResponse.getErrors().size()).isEqualTo(2);
        assertThat(actualResponse.getErrors()).containsExactlyInAnyOrder(
            "To continue, applicant 1 must believe and declare that their marriage has irrevocably broken",
            "To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");

    }

    @Test
    public void shouldAllowProgressIfMarriageIsBroken() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            AboutToStartOrSubmitResponse
                .<CaseData, State>builder()
                .errors(emptyList())
                .build();

        when(marriageIrretrievablyBrokenPage.midEvent(details, details)).thenReturn(response);

        AboutToStartOrSubmitResponse<CaseData, State> actualResponse =
            marriageIrretrievablyBrokenForApplicant2Page.midEvent(details, details);

        assertThat(actualResponse.getErrors().size()).isZero();
    }

    @Test
    public void shouldAllowProgressIfMarriageIsBrokenForBothApplicants() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            AboutToStartOrSubmitResponse
                .<CaseData, State>builder()
                .errors(emptyList())
                .build();

        when(marriageIrretrievablyBrokenPage.midEvent(details, details)).thenReturn(response);

        AboutToStartOrSubmitResponse<CaseData, State> actualResponse =
            marriageIrretrievablyBrokenForApplicant2Page.midEvent(details, details);

        assertThat(actualResponse.getErrors().size()).isZero();
    }
}
