package uk.gov.hmcts.divorce.solicitor.event.page.searchgov;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.solicitor.event.page.searchgov.SearchGovRespondentDetailsPage.ERROR_FUTURE_DOB;
import static uk.gov.hmcts.divorce.solicitor.event.page.searchgov.SearchGovRespondentDetailsPage.ERROR_NI_FORMAT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SearchGovRespondentDetailsPageTest {

    @InjectMocks
    private SearchGovRespondentDetailsPage page;

    @Test
    void shouldReturnErrorWhenPartnersDoBIsInTheFuture() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().setSearchGovRecordsJourneyOptions(
            SearchGovRecordsJourneyOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
            .setKnowPartnerDateOfBirth(YES);
        caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
            .setPartnerDateOfBirth(LocalDate.now().plusDays(1));

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
        caseData.getApplicant1().getInterimApplicationOptions().setSearchGovRecordsJourneyOptions(
            SearchGovRecordsJourneyOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
            .setKnowPartnerDateOfBirth(YES);
        caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
            .setPartnerDateOfBirth(LocalDate.now().minusYears(35));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorWhenPartnersNiIsIncorrect() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().setSearchGovRecordsJourneyOptions(
            SearchGovRecordsJourneyOptions.builder().build());
        caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
            .setKnowPartnerNationalInsurance(YES);
        caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
            .setPartnerNationalInsurance("AB123456");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).containsExactly(ERROR_NI_FORMAT);
    }

    static Stream<Arguments> provideTestArguments() {
        return Stream.of(
            Arguments.of("AB12 34 56 C", true),
            Arguments.of("AB123456C", true),
            Arguments.of("AB 12 34 56C", true),
            Arguments.of("aB12 34 56 C", true),
            Arguments.of("AB123456", false),
            Arguments.of("AB 12 34 56 C ", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestArguments")
    void shouldCheckIfNationalInsuranceIsValid(
        String nationalInsuranceNumber,
        boolean expectedOutcome
    ) {
        boolean result = page.isNationalInsuranceValid(nationalInsuranceNumber);
        AssertionsForClassTypes.assertThat(result).isEqualTo(expectedOutcome);
    }
}
