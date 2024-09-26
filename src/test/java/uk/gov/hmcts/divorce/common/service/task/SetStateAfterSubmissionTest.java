package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetStateAfterSubmissionTest {

    @InjectMocks
    private SetStateAfterSubmission setStateAfterSubmission;

    @Test
    void shouldSetAwaitingHwfDecisionStateIfSoleCitizenNeedsHelpWithFees() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(YES)
                .build())
            .build();

        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    void shouldSetAwaitingPaymentStateIfJointAndOnlyOneCitizenNeedsHelpWithFees() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(YES)
                .build())
            .applicant2HelpWithFees(HelpWithFees.builder()
                .needHelp(NO)
                .build())
            .build();

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetAwaitingHwfDecisionStateIfCitizenNeedsHelpWithFeesForJoint() {

        final Application application = Application.builder()
            .applicant1HelpWithFees(HelpWithFees.builder()
                .needHelp(YES)
                .build())
            .applicant2HelpWithFees(HelpWithFees.builder()
                .needHelp(YES)
                .build())
            .build();

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    void shouldSetAwaitingHwfDecisionStateIfSolicitorSetHelpWithFees() {

        final var application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    void shouldSetAwaitingHwfDecisionStateIfSolicitorSetHelpWithFeesForJoin() {

        final var application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    void shouldSetAwaitingPaymentStateIfApplicationHasNotBeenPaid() {

        final var application = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder()
                .paymentTotal("55000")
                .build())
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetAwaitingDocumentsStateIfApplicationIsAwaitingDocuments() {

        final var payment = new ListValue<>(null, Payment
            .builder()
            .amount(55000)
            .status(SUCCESS)
            .build());

        final var application = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder()
                .paymentTotal("55000")
                .build())
            .applicationPayments(singletonList(payment))
            .applicant1CannotUpload(YES)
            .serviceMethod(ServiceMethod.PERSONAL_SERVICE)
            .build();

        final var caseData = caseData();
        caseData.setApplication(application);
        caseData.setApplicationType(SOLE_APPLICATION);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingDocuments);
    }

    @Test
    void shouldSetSubmittedStateIfApplicationIsNotAwaitingDocuments() {

        final var payment = new ListValue<>(null, Payment
            .builder()
            .amount(55000)
            .status(SUCCESS)
            .build());

        final var application = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder()
                .paymentTotal("55000")
                .build())
            .applicationPayments(singletonList(payment))
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Submitted);
    }

    @Test
    public void givenSoleApplicationWithHWF_WhenAwaitingDocuments_ThenShouldSetAwaitingHWFDecisionState() {
        final var application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .applicant1CannotUpload(YES)
            .applicant1CannotUploadSupportingDocument(Set.of(MARRIAGE_CERTIFICATE))
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    public void givenSoleApplicationWithHWF_WhenNoAwaitingDocuments_ThenShouldSetAwaitingHWFDecisionState() {
        final var application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .applicant1CannotUpload(NO)
            .applicant1CannotUploadSupportingDocument(null)
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    public void givenJointApplicationWithHWF_WhenApplicant1AwaitingDocuments_ThenShouldSetAwaitingHWFDecisionState() {
        final var application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .applicant1CannotUpload(YES)
            .applicant1CannotUploadSupportingDocument(Set.of(MARRIAGE_CERTIFICATE))
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    public void givenJointApplicationWithHWF_WhenApplicantsUploadedRequiredDocs_ThenShouldSetAwaitingHWFDecisionState() {
        final var application = Application.builder()
            .solPaymentHowToPay(FEES_HELP_WITH)
            .applicant1CannotUpload(NO)
            .applicant1CannotUploadSupportingDocument(null)
            .applicant2CannotUpload(NO)
            .applicant2CannotUploadSupportingDocument(null)
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    public void givenJointApplicationWithoutHWF_WhenApplicant2AwaitingDocuments_ThenShouldSetAwaitingDocumentState() {
        final var payment = new ListValue<>(null, Payment
            .builder()
            .amount(55000)
            .status(SUCCESS)
            .build());

        final var application = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder()
                .paymentTotal("55000")
                .build())
            .applicationPayments(singletonList(payment))
            .applicant2CannotUpload(YES)
            .applicant2CannotUploadSupportingDocument(Set.of(MARRIAGE_CERTIFICATE))
            .build();

        final var caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplication(application);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingDocuments);
    }

    @Test
    void shouldSetWelshTranslationReviewStateForSoleApplicationIfApplicant1LanguagePreferenceWelshIsYes() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(WelshTranslationReview);
        assertThat(result.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetWelshTranslationReviewStateForJointApplicationIfapp1LanguagePreferenceWelshIsYes() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(WelshTranslationReview);
        assertThat(result.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetWelshTranslationReviewStateForJointApplicationIfApp2LanguagePreferenceWelshIsYes() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> result = setStateAfterSubmission.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(WelshTranslationReview);
        assertThat(result.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingPayment);
    }
}
