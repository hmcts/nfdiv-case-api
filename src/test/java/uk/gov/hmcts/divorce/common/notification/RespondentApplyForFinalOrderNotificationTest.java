package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(MockitoExtension.class)
class RespondentApplyForFinalOrderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RespondentApplyForFinalOrderNotification respondentApplyForFinalOrderNotification;

    @Test
    public void shouldNotSendRespondentApplyForFinalOrderEmailToApplicant1() {
        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        respondentApplyForFinalOrderNotification.sendToApplicant1(data, 1234567890123456L);

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldSendRespondentApplyForFinalOrderEmailToApplicant2() {
        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        respondentApplyForFinalOrderNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(RESPONDENT_APPLY_FOR_FINAL_ORDER),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    public void shouldSendRespondentApplyForFinalOrderEmailWithCorrectContent() {
        final var data = validCaseDataForAwaitingFinalOrder();
        final var applicant2 = getApplicant2(MALE);
        data.setApplicant2(applicant2);
        data.setApplicationType(SOLE_APPLICATION);

        when(commonContent.conditionalOrderTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        respondentApplyForFinalOrderNotification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            any(),
            any(),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.NO)
            )),
            any()
        );
    }
}
