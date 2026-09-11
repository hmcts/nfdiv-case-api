package uk.gov.hmcts.divorce.solicitor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_DETAILS_UPDATED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;

@ExtendWith(MockitoExtension.class)
class RespondentDetailsUpdatedNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RespondentDetailsUpdatedNotification notification;

    @Test
    void shouldSendApplicant1SolicitorNotification() {

        Long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder()
                        .email(TEST_SOLICITOR_EMAIL)
                        .build())
                    .build()
            )
            .build();

        when(commonContent.solicitorTemplateVars(caseData, caseId, caseData.getApplicant1())).thenReturn(getBasicTemplateVars());

        notification.sendToApplicant1Solicitor(caseData, caseId);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(RESPONDENT_DETAILS_UPDATED),
            any(),
            eq(ENGLISH),
            eq(caseId)
        );
        verifyNoMoreInteractions(notificationService);
        verify(commonContent).solicitorTemplateVars(caseData, caseId, caseData.getApplicant1());
    }
}
