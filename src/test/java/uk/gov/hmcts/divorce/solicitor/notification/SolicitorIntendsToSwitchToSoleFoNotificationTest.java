package uk.gov.hmcts.divorce.solicitor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.SwitchToSoleSolicitorTemplateContent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SolicitorIntendsToSwitchToSoleFoNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    SwitchToSoleSolicitorTemplateContent switchToSoleSolicitorTemplateContent;

    @InjectMocks
    private SolicitorIntendsToSwitchToSoleFoNotification solicitorIntendsToSwitchToSoleFoNotification;

    @Test
    void shouldSendApplicant1SolicitorNotificationIfApplicant1IsRepresented() {

        final Applicant applicant1 = Applicant.builder()
            .firstName("Julie")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app1 sol")
                    .reference("sol ref")
                    .build()
            )
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName("Bob")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app2 sol")
                    .reference("sol ref")
                    .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(
                applicant1
            )
            .applicant2(
                applicant2
            )
            .finalOrder(FinalOrder.builder().doesApplicant2IntendToSwitchToSole(YES).build())
            .build();


        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(switchToSoleSolicitorTemplateContent).templatevars(caseData,TEST_CASE_ID, applicant1, applicant2);
        verify(notificationService).sendEmail(
            eq(caseData.getApplicant1().getSolicitor().getEmail()),
            eq(OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR),
            anyMap(),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfApplicant2DidNotTriggerEvent() {

        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant2IntendToSwitchToSole(NO).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(switchToSoleSolicitorTemplateContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfApplicant2IsRepresented() {


        final Applicant applicant1 = Applicant.builder()
            .firstName("Julie")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app1 sol")
                    .build()
            )
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName("Bob")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app2 sol")
                    .build()
            )
            .build();
        final CaseData caseData = CaseData.builder()
            .applicant1(
                applicant1
            )
            .applicant2(
                applicant2
            )
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(YES).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(switchToSoleSolicitorTemplateContent).templatevars(caseData,TEST_CASE_ID,applicant2,applicant1);
        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getSolicitor().getEmail()),
            eq(OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfApplicant1DidNotTriggerEvent() {

        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(NO).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(switchToSoleSolicitorTemplateContent);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant2NotificationIfApplicant2IsNotRepresented() {


        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(YES).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(switchToSoleSolicitorTemplateContent).templatevars(eq(caseData),eq(TEST_CASE_ID),any(Applicant.class),any(Applicant.class));
        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getEmail()),
            eq(OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldNotSendApplicant2NotificationIfApplicant1DidNotTriggerEvent() {

        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(NO).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(switchToSoleSolicitorTemplateContent);
        verifyNoInteractions(notificationService);
    }
}
