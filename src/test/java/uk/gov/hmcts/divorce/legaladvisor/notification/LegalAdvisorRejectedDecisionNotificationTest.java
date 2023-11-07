package uk.gov.hmcts.divorce.legaladvisor.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderRefusalDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class LegalAdvisorRejectedDecisionNotificationTest {

    private static final DocumentPackInfo TEST_DOCUMENT_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT)),
        ImmutableMap.of(COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME)
    );

    @Mock
    private NotificationService notificationService;

    @Mock
    private ConditionalOrderRefusalDocumentPack conditionalOrderRefusalDocumentPack;

    @Mock
    private CommonContent commonContent;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private LegalAdvisorRejectedDecisionNotification notification;

    @Test
    void shouldSendConditionalOrderRefusedForAmendmentEmailToApplicant1() {

        final var data = validApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(REJECT)
            .build());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendConditionalOrderRefusedForAmendmentEmailInWelshToApplicant1WhenApplicant1() {

        final var data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(REJECT)
            .build());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendConditionalOrderRefusedForAmendmentEmailToApplicant2InJointApplication() {

        final var data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(REJECT)
            .build());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendConditionalOrderRefusedForAmendmentEmailInWelshToApplicant2InJointApplication() {

        final var data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(REJECT)
            .build());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendConditionalOrderRefusedEmailToApplicant2IfSole() {

        final var data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        notification.sendToApplicant2(data, TEST_CASE_ID);
        verifyNoInteractions(notificationService);
    }


    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant1Solicitor() {

        final var data = validApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(REJECT)
            .build());
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name("applicant solicitor")
            .reference("sol1")
            .email("sol1@gm.com")
            .build());

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("sol1@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).getCoRefusedSolicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), REJECT);
    }

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant2Solicitor() {

        final var data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(REJECT)
            .build());
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("applicant2 solicitor")
            .reference("sol2")
            .email("sol2@gm.com")
            .build());

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("sol2@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).getCoRefusedSolicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), REJECT);
    }

    @Test
    void shouldSendConditionalOrderRejectedLettersToApplicant1IfOffline() {
        CaseData caseData = validApplicant1CaseData();

        when(conditionalOrderRefusalDocumentPack.getDocumentPack(eq(caseData), eq(caseData.getApplicant1())))
            .thenReturn(TEST_DOCUMENT_PACK_INFO);
        when(conditionalOrderRefusalDocumentPack.getLetterId()).thenReturn(TEST_REFERENCE);

        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant1(), TEST_DOCUMENT_PACK_INFO, TEST_REFERENCE);
    }

    @Test
    void shouldSendConditionalOrderRejectedLettersToApplicant2IfOfflineAndJointApplication() {
        CaseData caseData = validJointApplicant1CaseData();

        when(conditionalOrderRefusalDocumentPack.getDocumentPack(caseData, caseData.getApplicant2()))
            .thenReturn(TEST_DOCUMENT_PACK_INFO);
        when(conditionalOrderRefusalDocumentPack.getLetterId()).thenReturn(TEST_REFERENCE);

        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(letterPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant2(), TEST_DOCUMENT_PACK_INFO, TEST_REFERENCE);
    }

    @Test
    void shouldNotSendConditionalOrderRejectedLettersToApplicant2IfOfflineAndSoleApplication() {
        CaseData caseData = validApplicant1CaseData();

        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verifyNoInteractions(letterPrinter);
    }
}
