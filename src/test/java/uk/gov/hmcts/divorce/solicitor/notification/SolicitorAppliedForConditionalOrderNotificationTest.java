package uk.gov.hmcts.divorce.solicitor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;

@ExtendWith(MockitoExtension.class)
class SolicitorAppliedForConditionalOrderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SolicitorAppliedForConditionalOrderNotification notification;

    @Test
    void shouldSendApplicant1SolicitorNotificationIfApplicantIsRepresentedAndIsJointApplication() {

        Long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder()
                        .email(TEST_SOLICITOR_EMAIL)
                        .reference("ref")
                        .name(TEST_SOLICITOR_NAME)
                        .build())
                    .firstName(TEST_FIRST_NAME)
                    .lastName(TEST_LAST_NAME)
                    .build()
            )
            .application(Application.builder()
                .issueDate(LocalDate.now())
                .build())
            .divorceOrDissolution(DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .build();

        when(commonContent.basicTemplateVars(caseData, caseId)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(caseId)).thenReturn(SIGN_IN_DIVORCE_TEST_URL);

        notification.sendToApplicant1Solicitor(caseData, caseId);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
            any(),
            eq(ENGLISH),
            eq(caseId)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(caseData, caseId);
        verify(commonContent).getProfessionalUsersSignInUrl(caseId);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfApplicantIsRepresentedAndIsJointApplication() {

        Long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant2(
                Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder()
                        .email(TEST_SOLICITOR_EMAIL)
                        .reference("ref")
                        .name(TEST_SOLICITOR_NAME)
                        .build())
                    .firstName(TEST_FIRST_NAME)
                    .lastName(TEST_LAST_NAME)
                    .build()
            )
            .application(Application.builder()
                .issueDate(LocalDate.now())
                .build())
            .divorceOrDissolution(DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .build();

        when(commonContent.basicTemplateVars(caseData, caseId)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(caseId)).thenReturn(SIGN_IN_DIVORCE_TEST_URL);

        notification.sendToApplicant2Solicitor(caseData, caseId);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_SOLICITOR_BOTH_APPLIED_CO_FO),
            any(),
            eq(ENGLISH),
            eq(caseId)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).basicTemplateVars(caseData, caseId);
        verify(commonContent).getProfessionalUsersSignInUrl(caseId);
    }
}
