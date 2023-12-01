package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ApplyForFinalOrderDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.UNION_TYPE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLY_FOR_FINAL_ORDER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class AwaitingFinalOrderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplyForFinalOrderDocumentPack applyForFinalOrderDocumentPack;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private AwaitingFinalOrderNotification awaitingFinalOrderNotification;

    @Test
    void shouldSendAwaitingFinalOrderEmailToApplicant1IfNotRepresentedAndSole() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        awaitingFinalOrderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAwaitingFinalOrderEmailToApplicant1IfNotRepresentedAndJoint() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAwaitingFinalOrderEmailToApplicant1IfNotRepresentedAndJointInWelsh() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAwaitingFinalOrderEmailToApplicant1IfNotRepresentedAndSoleInWelsh() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        awaitingFinalOrderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.NO)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldNotSendAwaitingFinalOrderEmailToApplicant2IfNotRepresentedAndSole() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        awaitingFinalOrderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendAwaitingFinalOrderEmailToApplicant2IfNotRepresentedAndJoint() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAwaitingFinalOrderEmailToApplicant2IfNotRepresentedAndJointInWelsh() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        awaitingFinalOrderNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAwaitingFinalOrderEmailWithCorrectTemplateVars() {

        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        awaitingFinalOrderNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_APPLY_FOR_FINAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(IS_REMINDER, CommonContent.NO),
                hasEntry(DATE_FINAL_ORDER_ELIGIBLE_FROM_PLUS_3_MONTHS, "21 March 2022")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorJointNotification() {
        final var applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .applicant1(applicant)
            .build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        LocalDate coGrantedDate = LocalDate.of(2021, 10, 18);
        data.getFinalOrder().setDateFinalOrderEligibleFrom(coGrantedDate);

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getBasicTemplateVars());
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        awaitingFinalOrderNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLY_FOR_FINAL_ORDER_SOLICITOR),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(UNION_TYPE, DIVORCE),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER)),
                hasEntry(SOLICITOR_REFERENCE, "not provided"),
                hasEntry(IS_SOLE, YES),
                hasEntry(IS_JOINT, NO),
                hasEntry(FINAL_ORDER_OVERDUE_DATE, coGrantedDate.plusMonths(12).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorJointNotificationWithReference() {
        final var applicant = getApplicant();
        applicant.setSolicitor(Solicitor.builder().reference("ref").email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant.setSolicitorRepresented(YesOrNo.YES);
        final var data = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .applicant2(applicant)
            .build();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        LocalDate coGrantedDate = LocalDate.of(2021, 10, 18);
        data.getFinalOrder().setDateFinalOrderEligibleFrom(coGrantedDate);

        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getBasicTemplateVars());
        when(commonContent.getUnionType(data)).thenReturn(DIVORCE);

        awaitingFinalOrderNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLY_FOR_FINAL_ORDER_SOLICITOR),
            argThat(allOf(
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(APPLICANT_NAME, String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                hasEntry(RESPONDENT_NAME, String.join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME)),
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(UNION_TYPE, DIVORCE),
                hasEntry(DATE_OF_ISSUE, LocalDate.of(2021, 6, 18).format(DATE_TIME_FORMATTER)),
                hasEntry(SOLICITOR_REFERENCE, "ref"),
                hasEntry(IS_SOLE, NO),
                hasEntry(IS_JOINT, YES),
                hasEntry(FINAL_ORDER_OVERDUE_DATE, coGrantedDate.plusMonths(12).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendCanApplyForFinalOrderLettersToOfflineApplicant1() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);

        DocumentPackInfo documentPackInfo = mock(DocumentPackInfo.class);

        when(applyForFinalOrderDocumentPack.getDocumentPack(
            caseData,
            caseData.getApplicant1())).thenReturn(documentPackInfo);

        awaitingFinalOrderNotification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verify(applyForFinalOrderDocumentPack).getDocumentPack(
            caseData,
            caseData.getApplicant1());

        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            documentPackInfo,
            applyForFinalOrderDocumentPack.getLetterId());
    }

    @Test
    void shouldSendCanApplyForFinalOrderLettersToOfflineApplicant2InJointApplication() {
        CaseData caseData = validJointApplicant1CaseData();

        DocumentPackInfo documentPackInfo = mock(DocumentPackInfo.class);

        when(applyForFinalOrderDocumentPack.getDocumentPack(
            caseData,
            caseData.getApplicant2())).thenReturn(documentPackInfo);

        awaitingFinalOrderNotification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(applyForFinalOrderDocumentPack).getDocumentPack(
            caseData,
            caseData.getApplicant2());

        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            documentPackInfo,
            applyForFinalOrderDocumentPack.getLetterId());
    }

    @Test
    void shouldNotSendCanApplyForFinalOrderLettersToOfflineApplicant2InSoleApplication() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);

        awaitingFinalOrderNotification.sendToApplicant2Offline(caseData, TEST_CASE_ID);
    }

}
