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
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.solicitor.notification.SolicitorIntendsToSwitchToSoleFoNotification.APPLICANT_1_NAME;
import static uk.gov.hmcts.divorce.solicitor.notification.SolicitorIntendsToSwitchToSoleFoNotification.APPLICANT_2_NAME;
import static uk.gov.hmcts.divorce.solicitor.notification.SolicitorIntendsToSwitchToSoleFoNotification.DATE_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SolicitorIntendsToSwitchToSoleFoNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Clock clock;

    @InjectMocks
    private SolicitorIntendsToSwitchToSoleFoNotification solicitorIntendsToSwitchToSoleFoNotification;

    @Test
    void shouldSendApplicant1SolicitorNotificationIfApplicant1IsRepresented() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName("Julie")
                    .lastName("Smith")
                    .solicitor(
                        Solicitor.builder()
                            .name("app1 sol")
                            .reference("sol ref")
                            .build()
                    )
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .solicitor(
                        Solicitor.builder()
                            .name("app2 sol")
                            .reference("sol ref")
                            .build()
                    )
                    .build()
            )
            .finalOrder(FinalOrder.builder().doesApplicant2IntendToSwitchToSole(YES).build())
            .build();

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICANT_1_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateContent.put(APPLICANT_2_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateContent.put(SOLICITOR_REFERENCE, "sol ref");
        templateContent.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR,
            templateContent,
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfApplicant2DidNotTriggerEvent() {

        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant2IntendToSwitchToSole(NO).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant1Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfApplicant2IsRepresented() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName("Julie")
                    .lastName("Smith")
                    .solicitor(
                        Solicitor.builder()
                            .name("app1 sol")
                            .build()
                    )
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .solicitor(
                        Solicitor.builder()
                            .name("app2 sol")
                            .build()
                    )
                    .build()
            )
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(YES).build())
            .build();

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(APPLICANT_1_NAME,
            join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateContent.put(APPLICANT_2_NAME,
            join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        templateContent.put(SOLICITOR_REFERENCE, NOT_PROVIDED);
        templateContent.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR,
            templateContent,
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfApplicant1DidNotTriggerEvent() {

        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(NO).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant2NotificationIfApplicant2IsNotRepresented() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(YES).build())
            .build();

        Map<String, String> templateContent = new HashMap<>();
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            caseData.getApplicant2().getEmail(),
            OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_CITIZEN,
            templateContent,
            caseData.getApplicant2().getLanguagePreference()
        );
    }

    @Test
    void shouldNotSendApplicant2NotificationIfApplicant1DidNotTriggerEvent() {

        final CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(NO).build())
            .build();

        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }
}
