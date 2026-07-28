package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.SoleApplicantFinalOrderOverdueDocumentPack;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(MockitoExtension.class)
class SoleApplicantFinalOrderOverdueNotificationTest {

    @Mock
    private SoleApplicantFinalOrderOverdueDocumentPack soleApplicantFinalOrderOverdueDocumentPack;

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private SoleApplicantFinalOrderOverdueNotification soleApplicantFinalOrderOverdueNotification;

    @Test
    void shouldSendSoleApplicantFinalOrderOverdueEmail() {
        final var data = validCaseDataForAwaitingFinalOrder();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplicant1().setEmail(TEST_USER_EMAIL);

        soleApplicantFinalOrderOverdueNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOLE_APPLICANT_FINAL_ORDER_OVERDUE),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendSoleApplicantSolicitorFinalOrderOverdueEmail() {
        final var data = validCaseDataForAwaitingFinalOrder();
        data.getApplication().setIssueDate(LocalDate.of(2021, 4, 5));
        data.getConditionalOrder().setGrantedDate(LocalDate.of(2021, 6, 5));
        data.getApplicant1().setSolicitor(
            Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).reference("1234").build());
        data.setApplicationType(SOLE_APPLICATION);

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID, data.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID))
            .thenReturn("test-url");

        soleApplicantFinalOrderOverdueNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SOLE_APPLICANT_SOLICITOR_FINAL_ORDER_OVERDUE),
            argThat(allOf(
                hasEntry(DATE_OF_ISSUE, "5 April 2021"),
                hasEntry(FINAL_ORDER_OVERDUE_DATE, "5 June 2022"),
                hasEntry(SIGN_IN_URL, "test-url"),
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(SOLICITOR_REFERENCE, "1234")
            )),
            eq(data.getApplicant1().getLanguagePreference()),
            any()
        );
    }

    @Test
    void shouldSendSoleApplicantFinalOrderOverdueOfflineNotification() {
        final var data = validCaseDataForAwaitingFinalOrder();
        data.setApplicationType(SOLE_APPLICATION);

        DocumentPackInfo documentPackInfo = mock(DocumentPackInfo.class);

        when(soleApplicantFinalOrderOverdueDocumentPack.getDocumentPack(
            data,
            data.getApplicant1())).thenReturn(documentPackInfo);

        soleApplicantFinalOrderOverdueNotification.sendToApplicant1Offline(data, TEST_CASE_ID);

        verify(soleApplicantFinalOrderOverdueDocumentPack).getDocumentPack(
            data,
            data.getApplicant1());

        verify(letterPrinter).sendLetters(
            data,
            TEST_CASE_ID,
            data.getApplicant1(),
            documentPackInfo,
            soleApplicantFinalOrderOverdueDocumentPack.getLetterId());
    }
}
