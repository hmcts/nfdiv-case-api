package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.BAILIFF_SERVICE_UNSUCCESSFUL;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class BailiffServiceUnsuccessfulNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private BailiffServiceUnsuccessfulNotification notification;

    @Test
    void shouldSendNotificationToApplicantWithDivorceContent() {
        CaseData caseData = validApplicant1CaseData();
        when(commonContent.mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(BAILIFF_SERVICE_UNSUCCESSFUL),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicantWithDissolutionContent() {
        CaseData caseData = validApplicant1CaseData();
        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(IS_DISSOLUTION, YES);
        templateVars.put(IS_DIVORCE, NO);
        when(commonContent.mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(caseData, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(BAILIFF_SERVICE_UNSUCCESSFUL),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(caseData, 1234567890123456L, caseData.getApplicant1(), caseData.getApplicant2());
    }
}
