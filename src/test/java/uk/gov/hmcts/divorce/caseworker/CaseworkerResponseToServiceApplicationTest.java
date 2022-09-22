package uk.gov.hmcts.divorce.caseworker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.event.CaseworkerResponseToServiceApplication;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceOutcome;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerResponseToServiceApplication.CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class CaseworkerResponseToServiceApplicationTest {

    private static final String ALTERNATIVE_SERVICE_TYPE_NULL_ERROR = "Please set the alternative service type before using this event";

    @InjectMocks
    private CaseworkerResponseToServiceApplication caseworkerResponseToServiceApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerResponseToServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION);
    }

    @Test
    void shouldMoveStateToAwaitingServiceConsiderationWhenDeemed() {

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService
                .builder()
                .alternativeServiceType(DEEMED)
                .build())
            .build();

        var alternativeServiceOutcomeListValue = ListValue
            .<AlternativeServiceOutcome>builder()
            .value(caseData.getAlternativeService().getOutcome())
            .build();

        List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes = new ArrayList<>();
        alternativeServiceOutcomes.add(0, alternativeServiceOutcomeListValue);
        caseData.setAlternativeServiceOutcomes(alternativeServiceOutcomes);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToSubmit(
            updatedCaseDetails,
            CaseDetails.<CaseData, State>builder().build()
        );

        assertThat(response.getState()).isEqualTo(State.AwaitingServiceConsideration);
    }

    @Test
    void shouldMoveStateToAwaitingServiceConsiderationWhenDispensed() {

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService
                .builder()
                .alternativeServiceType(DISPENSED)
                .build())
            .build();

        var alternativeServiceOutcomeListValue = ListValue
            .<AlternativeServiceOutcome>builder()
            .value(caseData.getAlternativeService().getOutcome())
            .build();

        List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes = new ArrayList<>();
        alternativeServiceOutcomes.add(0, alternativeServiceOutcomeListValue);
        caseData.setAlternativeServiceOutcomes(alternativeServiceOutcomes);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToSubmit(
            updatedCaseDetails,
            CaseDetails.<CaseData, State>builder().build()
        );

        assertThat(response.getState()).isEqualTo(State.AwaitingServiceConsideration);
    }

    @Test
    void shouldMoveStateToAwaitingBailiffReferralWhenBailiff() {

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService
                .builder()
                .alternativeServiceType(BAILIFF)
                .build())
            .build();

        var alternativeServiceOutcomeListValue = ListValue
            .<AlternativeServiceOutcome>builder()
            .value(caseData.getAlternativeService().getOutcome())
            .build();

        List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes = new ArrayList<>();
        alternativeServiceOutcomes.add(0, alternativeServiceOutcomeListValue);
        caseData.setAlternativeServiceOutcomes(alternativeServiceOutcomes);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToSubmit(
            updatedCaseDetails,
            CaseDetails.<CaseData, State>builder().build()
        );

        assertThat(response.getState()).isEqualTo(State.AwaitingBailiffReferral);
    }

    @Test
    void shouldNotReturnValidationErrorsWhenAlternativeServiceTypeListIsPopulated() {

        List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes =
            List.of(ListValue.<AlternativeServiceOutcome>builder()
                .value(
                    AlternativeServiceOutcome.builder()
                        .alternativeServiceType(BAILIFF)
                        .build())
                .build()
            );
        CaseData caseData = CaseData.builder()
            .alternativeServiceOutcomes(alternativeServiceOutcomes)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setState(AosOverdue);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToStart(updatedCaseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnValidationErrorWhenAlternativeServiceTypeListIsNull() {

        CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setState(AosOverdue);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToStart(updatedCaseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(ALTERNATIVE_SERVICE_TYPE_NULL_ERROR);
    }

    @Test
    void shouldReturnValidationErrorWhenAlternativeServiceTypeListIsEmpty() {

        List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes = Collections.emptyList();
        CaseData caseData = CaseData.builder()
            .alternativeServiceOutcomes(alternativeServiceOutcomes)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setState(AosOverdue);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToStart(updatedCaseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(ALTERNATIVE_SERVICE_TYPE_NULL_ERROR);
    }

    @Test
    void shouldReturnValidationErrorWhenAlternativeServiceTypeInListIsNull() {

        List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes =
            List.of(ListValue.<AlternativeServiceOutcome>builder()
                .value(AlternativeServiceOutcome.builder().build())
                .build()
            );
        CaseData caseData = CaseData.builder()
            .alternativeServiceOutcomes(alternativeServiceOutcomes)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setState(AosOverdue);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToStart(updatedCaseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(ALTERNATIVE_SERVICE_TYPE_NULL_ERROR);
    }
}
