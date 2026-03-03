package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.FEES_CONSENT_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.FEES_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINANCIAL_ORDER_REQUESTED_NOTIFICATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_FINANCIAL_ORDER_REQUESTED_NOTIFICATION;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForFinancialOrder;

@ExtendWith(MockitoExtension.class)
public class FinancialOrderRequestedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private FinancialOrderRequestedNotification financialOrderRequestedNotification;

    @Test
    void shouldSendEmailToApplicant1WithSoleAndJointFinancialOrderContent() {
        CaseData data = validCaseDataForFinancialOrder();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        Map<String, String> divorceTemplateVars = new HashMap<>();
        divorceTemplateVars.putAll(getMainTemplateVars());
        divorceTemplateVars.put(FEES_FINANCIAL_ORDER, formatAmount(275.0));
        divorceTemplateVars.put(FEES_CONSENT_ORDER, formatAmount(58.0));
        when(paymentService.getServiceCost(anyString(), anyString(), anyString())).thenReturn(275.0);
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        financialOrderRequestedNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(FINANCIAL_ORDER_REQUESTED_NOTIFICATION),
            eq(divorceTemplateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithSoleAndJointFinancialOrderContent() {
        CaseData data = validCaseDataForFinancialOrder();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);

        Map<String, String> divorceTemplateVars = new HashMap<>();
        divorceTemplateVars.putAll(getMainTemplateVars());
        divorceTemplateVars.put(FEES_FINANCIAL_ORDER, formatAmount(275.0));
        divorceTemplateVars.put(FEES_CONSENT_ORDER, formatAmount(58.0));
        when(paymentService.getServiceCost(anyString(), anyString(), anyString())).thenReturn(275.0);
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        financialOrderRequestedNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(FINANCIAL_ORDER_REQUESTED_NOTIFICATION),
            eq(divorceTemplateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithRespondentFinancialOrderContent() {
        CaseData data = validCaseDataForFinancialOrder();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);

        Map<String, String> divorceTemplateVars = new HashMap<>();
        divorceTemplateVars.putAll(getMainTemplateVars());
        divorceTemplateVars.put(FEES_FINANCIAL_ORDER, formatAmount(275.0));
        divorceTemplateVars.put(FEES_CONSENT_ORDER, formatAmount(58.0));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        financialOrderRequestedNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(RESPONDENT_FINANCIAL_ORDER_REQUESTED_NOTIFICATION),
            eq(divorceTemplateVars),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }
}
