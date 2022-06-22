package uk.gov.hmcts.divorce.legaladvisor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorCoRefusalDecisionNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private LegalAdvisorCoRefusalDecisionNotification coRefusalDecisionNotification;

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant1SolicitorWhenApplicantRepresentedInSoleApplication() {

        final var data = validApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
                .refusalDecision(RefusalOption.REJECT)
            .build());
        data.getApplication().setIssueDate(LocalDate.of(2022, 6, 22));
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
                .name("applicant solicitor")
                .reference("sol1")
                .email("sol1@gm.com")
            .build());

        when(commonContent.solicitorTemplateVars(data, 1234567890123456L, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));

        coRefusalDecisionNotification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq("sol1@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry("isJoint", NO),
                hasEntry("moreInfo", NO),
                hasEntry("amendApplication", YES),
                hasEntry("solicitor name", "applicant solicitor"),
                hasEntry("solicitorReference", "sol1"),
                hasEntry("applicant1Label", "Applicant"),
                hasEntry("applicant2Label", "Respondent"),
                hasEntry("issueDate", "22 June 2022"),
                hasEntry("signin url", "divorceTestUrl")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).solicitorTemplateVars(data, 1234567890123456L, data.getApplicant1());
    }

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant2SolicitorWhenApplicant2RepresentedInJointApplication() {

        final var data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(RefusalOption.MORE_INFO)
            .build());
        data.getApplication().setIssueDate(LocalDate.of(2022, 6, 22));
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("applicant2 solicitor")
            .reference("sol2")
            .email("sol2@gm.com")
            .build());

        when(commonContent.solicitorTemplateVars(data, 1234567890123456L, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));

        coRefusalDecisionNotification.sendToApplicant2Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq("sol2@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry("isJoint", YES),
                hasEntry("moreInfo", YES),
                hasEntry("amendApplication", NO),
                hasEntry("solicitor name", "applicant2 solicitor"),
                hasEntry("solicitorReference", "sol2"),
                hasEntry("applicant1Label", "Applicant 1"),
                hasEntry("applicant2Label", "Applicant 2"),
                hasEntry("issueDate", "22 June 2022"),
                hasEntry("signin url", "divorceTestUrl")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).solicitorTemplateVars(data, 1234567890123456L, data.getApplicant2());
    }
}
