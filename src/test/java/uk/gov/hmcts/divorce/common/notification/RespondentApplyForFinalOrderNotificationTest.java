package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ApplyForFinalOrderDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.GENERAL_FEE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(MockitoExtension.class)
class RespondentApplyForFinalOrderNotificationTest {

    @Mock
    private ApplyForFinalOrderDocumentPack applyForFinalOrderDocumentPack;

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private RespondentApplyForFinalOrderNotification respondentApplyForFinalOrderNotification;

    @Test
    void shouldNotSendRespondentApplyForFinalOrderEmailToApplicant1() {
        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        respondentApplyForFinalOrderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendRespondentApplyForFinalOrderEmailToApplicant2() {
        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        respondentApplyForFinalOrderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(RESPONDENT_APPLY_FOR_FINAL_ORDER),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }


    @Test
    void shouldSendRespondentApplyForFinalOrderEmailWithCorrectContent() {
        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        Double expectedGeneralAppFees = 180.00;
        when(paymentService.getServiceCost(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE)).thenReturn(expectedGeneralAppFees);
        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));
        when(commonContent.getSmartSurvey())
            .thenReturn("https://www.smartsurvey.co.uk/s/NFD_Feedback/?pageurl=email");

        respondentApplyForFinalOrderNotification.sendToApplicant2(data, TEST_CASE_ID);
        String formattedGeneralAppFees = "£180.00";
        verify(notificationService).sendEmail(
            any(),
            any(),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.NO),
                hasEntry(GENERAL_FEE, formattedGeneralAppFees),
                hasEntry(SMART_SURVEY, "https://www.smartsurvey.co.uk/s/NFD_Feedback/?pageurl=email")
            )),
            any(),
            any()
        );
    }

    @Test
    void shouldSendRespondentApplyForFinalOrderEmailToApplicant2Solicitor() {
        final var data = validCaseDataForAwaitingFinalOrder();
        data.getApplication().setIssueDate(LocalDate.of(2021, 4, 5));
        final var applicant2 = getApplicant2(MALE);
        applicant2.setSolicitor(
            Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).reference("1234").build());
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID))
            .thenReturn("test-url");

        respondentApplyForFinalOrderNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(RESPONDENT_SOLICITOR_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(ISSUE_DATE, "5 April 2021"),
                hasEntry(SIGN_IN_URL, "test-url"),
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(SOLICITOR_REFERENCE, "1234")
            )),
            eq(applicant2.getLanguagePreference()),
            any()
        );
    }

    @Test
    void shouldSendRespondentApplyForFinalOrderOfflineNotificationForSole() {
        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        DocumentPackInfo documentPackInfo = mock(DocumentPackInfo.class);

        when(applyForFinalOrderDocumentPack.getDocumentPack(
            data,
            data.getApplicant2())).thenReturn(documentPackInfo);

        respondentApplyForFinalOrderNotification.sendToApplicant2Offline(data, TEST_CASE_ID);

        verify(applyForFinalOrderDocumentPack).getDocumentPack(
            data,
            data.getApplicant2());

        verify(letterPrinter).sendLetters(
            data,
            TEST_CASE_ID,
            data.getApplicant2(),
            documentPackInfo,
            applyForFinalOrderDocumentPack.getLetterId());
    }
}
