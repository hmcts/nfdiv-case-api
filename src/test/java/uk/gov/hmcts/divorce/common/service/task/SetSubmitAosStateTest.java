package uk.gov.hmcts.divorce.common.service.task;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAnswer;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.NA;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetSubmitAosStateTest {

    @InjectMocks
    private SetSubmitAosState setSubmitAosState;

    @Test
    void shouldNotSetStateToHoldingIfPreviousStateIsNotInAnyOfTheServiceApplicationProcess() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AwaitingConditionalOrder);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingConditionalOrder);
    }

    @Test
    void shouldSetStateToAwaitingAnswerIfJsApplicationIsIsDisputed() {
        final CaseData caseData = caseData();
        caseData.setAcknowledgementOfService(
            AcknowledgementOfService.builder().howToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE).build()
        );
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .state(OfflineDocumentReceived)
            .build();

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingAnswer);
    }

    @Test
    void shouldSetStateToAwaitingJsNullityIfJsApplicationIsUndisputed() {
        final CaseData caseData = caseData();
        caseData.setAcknowledgementOfService(
            AcknowledgementOfService.builder().howToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE).build()
        );
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .state(OfflineDocumentReceived)
            .build();

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingJsNullity);
    }

    @Test
    void shouldKeepOriginalStateForNoQualifyingStates() {
        final CaseData caseData = caseData();
        caseData.setAcknowledgementOfService(
            AcknowledgementOfService.builder().howToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE).build()
        );
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .state(Draft)
            .build();

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Draft);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfRespondentLanguagePreferenceWelshIsYes() {
        final CaseData caseData = caseData();
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(NA);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(WelshTranslationReview);
        assertThat(result.getData().getApplication().getWelshPreviousState()).isEqualTo(Holding);
    }

    @Test
    void shouldNotSetStateToWelshTranslationReviewIfRespondentLanguagePreferenceWelshIsNo() {
        final CaseData caseData = caseData();
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(NA);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfRespondentUsedWelshTranslationOnSubmissionYes() {
        final CaseData caseData = caseData();
        caseData.getApplicant2().setUsedWelshTranslationOnSubmission(YES);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(NA);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(WelshTranslationReview);
        assertThat(result.getData().getApplication().getWelshPreviousState()).isEqualTo(Holding);
    }

    @Test
    void shouldNotSetStateToWelshTranslationReviewIfRespondentUsedWelshTranslationOnSubmissionNo() {
        final CaseData caseData = caseData();
        caseData.getApplicant2().setUsedWelshTranslationOnSubmission(NO);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(NA);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
    }

    @ParameterizedTest
    @MethodSource("caseStateParameters")
    public void shouldSetStateToHoldingIfPreviousStateIsInAnyOfTheServiceApplicationProcess(State aosValidState) {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(NA);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(aosValidState);
        assertThat(setSubmitAosState.apply(caseDetails).getState()).isEqualTo(Holding);
    }

    private static Stream<Arguments> caseStateParameters() {
        return Arrays.stream(ArrayUtils.addAll(AOS_STATES, AosDrafted, AosOverdue, OfflineDocumentReceived, AwaitingService))
            .filter(state -> !AwaitingConditionalOrder.equals(state))
            .map(Arguments::of);
    }
}
