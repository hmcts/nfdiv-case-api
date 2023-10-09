package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class PartnerNotAppliedForFinalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private PartnerNotAppliedForFinalOrderNotification notification;

    @Test
    void shouldSendEnglishEmailToApplicant1IfFirstInTime() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().email(TEST_USER_EMAIL).build())
            .applicant2(Applicant.builder().build())
            .finalOrder(FinalOrder.builder().applicant1AppliedForFinalOrderFirst(YES).build())
            .build();
        final Map<String, String> templateVars = new HashMap<>();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendEnglishEmailToApplicant1SolicitorIfFirstInTime() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .solicitor(Solicitor.builder()
                    .email(TEST_SOLICITOR_EMAIL)
                    .reference(TEST_REFERENCE)
                    .build())
                .build())
            .applicant2(Applicant.builder().solicitorRepresented(NO).build())
            .finalOrder(FinalOrder.builder().applicant1AppliedForFinalOrderFirst(YES).build())
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = new HashMap<>();
        when(commonContent.solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1());
    }

    @Test
    void shouldSendWelshEmailToApplicant1IfFirstInTime() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().email(TEST_USER_EMAIL).languagePreferenceWelsh(YES).build())
            .applicant2(Applicant.builder().build())
            .finalOrder(FinalOrder.builder().applicant1AppliedForFinalOrderFirst(YES).build())
            .build();
        final Map<String, String> templateVars = new HashMap<>();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER),
            eq(templateVars),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldNotSendEmailIfApplicant1WasNotFirstInTimeFinalOrderApplicant() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .finalOrder(FinalOrder.builder().applicant1AppliedForFinalOrderFirst(NO).build())
            .build();

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailIfApplicant1SolicitorWasNotFirstInTimeFinalOrderApplicant() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .finalOrder(FinalOrder.builder().applicant1AppliedForFinalOrderFirst(NO).build())
            .build();

        notification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendApplicant1EmailIfApplicationTypeIsSole() {
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .build();

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendApplicant1SolicitorEmailIfApplicationTypeIsSole() {
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .build();

        notification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEnglishEmailToApplicant2IfFirstInTime() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().email(TEST_USER_EMAIL).build())
            .finalOrder(FinalOrder.builder().applicant2AppliedForFinalOrderFirst(YES).build())
            .build();
        final Map<String, String> templateVars = new HashMap<>();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendEnglishEmailToApplicant2SolicitorIfFirstInTime() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant2(Applicant.builder()
                .solicitor(Solicitor.builder()
                    .email(TEST_SOLICITOR_EMAIL)
                    .reference(TEST_REFERENCE)
                    .build())
                .build())
            .applicant1(Applicant.builder().solicitorRepresented(NO).build())
            .finalOrder(FinalOrder.builder().applicant2AppliedForFinalOrderFirst(YES).build())
            .divorceOrDissolution(DIVORCE)
            .build();

        final Map<String, String> templateVars = new HashMap<>();
        when(commonContent.solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE_FINAL_ORDER),
            eq(templateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).solicitorTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2());
    }

    @Test
    void shouldSendWelshEmailToApplicant2IfFirstInTime() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().email(TEST_USER_EMAIL).languagePreferenceWelsh(YES).build())
            .finalOrder(FinalOrder.builder().applicant2AppliedForFinalOrderFirst(YES).build())
            .build();
        final Map<String, String> templateVars = new HashMap<>();

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OTHER_APPLICANT_NOT_APPLIED_FOR_FINAL_ORDER),
            eq(templateVars),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldNotSendEmailIfApplicant2WasNotFirstInTimeFinalOrderApplicant() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .finalOrder(FinalOrder.builder().applicant2AppliedForFinalOrderFirst(NO).build())
            .build();

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailIfApplicant2SolicitorWasNotFirstInTimeFinalOrderApplicant() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .finalOrder(FinalOrder.builder().applicant2AppliedForFinalOrderFirst(NO).build())
            .build();

        notification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendApplicant2EmailIfApplicationTypeIsSole() {
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .build();

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendApplicant2SolicitorEmailIfApplicationTypeIsSole() {
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .build();

        notification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent);
        verifyNoInteractions(notificationService);
    }
}
