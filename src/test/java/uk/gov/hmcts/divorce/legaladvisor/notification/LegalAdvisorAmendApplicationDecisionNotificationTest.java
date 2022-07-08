package uk.gov.hmcts.divorce.legaladvisor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorAmendApplicationDecisionNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private LegalAdvisorAmendApplicationDecisionNotification coRefusalDecisionNotification;

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

        coRefusalDecisionNotification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq("sol1@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            anyMap(),
            eq(ENGLISH)
        );

        verify(commonContent).getCoRefusedSolicitorTemplateVars(data, 1234567890123456L, data.getApplicant1(), REJECT);
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

        coRefusalDecisionNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq("sol2@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            anyMap(),
            eq(ENGLISH)
        );
        verify(commonContent).getCoRefusedSolicitorTemplateVars(data, 1234567890123456L, data.getApplicant2(), REJECT);
    }
}
