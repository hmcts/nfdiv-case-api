package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_REJECTED_FEE_NOT_PAID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ApplicationRejectedFeeNotPaidNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationRejectedFeeNotPaidNotification notificationHandler;

    @Test
    void shouldSendToApplicant1() {
        CaseData caseData = caseData();
        Long id = 1L;

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(id);
        caseDetails.setState(State.AwaitingPayment);
        caseDetails.setData(caseData);

        var templateVars = getTemplateVars(caseData, caseData.getApplicant1());
        when(commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()
        )).thenReturn(templateVars);

        notificationHandler.sendToApplicant1(caseDetails);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant1().getEmail()),
            eq(APPLICATION_REJECTED_FEE_NOT_PAID),
            anyMap(),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(id)
        );

        verify(commonContent).mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendToApplicant2WhenInJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        Long id = 1L;
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(id);
        caseDetails.setState(State.AwaitingPayment);
        caseDetails.setData(caseData);

        var templateVars = getTemplateVars(caseData, caseData.getApplicant2());
        when(commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1()
        )).thenReturn(templateVars);

        notificationHandler.sendToApplicant2(caseDetails);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICATION_REJECTED_FEE_NOT_PAID),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(id)
        );

        verify(commonContent).mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldNotSendToApplicant2ForSoleCases() {
        CaseData caseData = caseData();
        Long id = 1L;

        notificationHandler.sendToApplicant2(caseData, id);

        verifyNoInteractions(notificationService);

        verifyNoInteractions(commonContent);
    }

    private Map<String, String> getTemplateVars(CaseData caseData, Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(NAME, applicant.getFullName());
        templateVars.put(CASE_REFERENCE, TEST_CASE_ID.toString());
        templateVars.put(PARTNER, commonContent.getPartner(caseData, applicant, applicant.getLanguagePreference()));
        templateVars.put(SMART_SURVEY, SMART_SURVEY);
        return templateVars;
    }
}
