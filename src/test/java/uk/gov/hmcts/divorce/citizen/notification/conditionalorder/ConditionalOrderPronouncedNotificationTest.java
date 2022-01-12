package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_PRONOUNCED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(SpringExtension.class)
class ConditionalOrderPronouncedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private ConditionalOrderPronouncedNotification notification;

    private static final String COURT_NAME = "court name";
    private static final String DATE_OF_HEARING = "date of hearing";
    private static final String CO_FINAL_ORDER_FROM_DATE = "CO pronouncement date plus 43 days";

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMIGHAM)
            .dateAndTimeOfHearing(LocalDateTime.now())
            .pronouncedDate(LocalDate.now())
            .build());
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMIGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, data.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER)),
                hasEntry(CO_FINAL_ORDER_FROM_DATE, data.getConditionalOrder().getPronouncedDate().plusDays(43).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant1CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setConditionalOrder(ConditionalOrder.builder()
            .court(ConditionalOrderCourt.BIRMIGHAM)
            .dateAndTimeOfHearing(LocalDateTime.now())
            .pronouncedDate(LocalDate.now())
            .build());

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_PRONOUNCED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(COURT_NAME, ConditionalOrderCourt.BIRMIGHAM.getLabel()),
                hasEntry(DATE_OF_HEARING, data.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER)),
                hasEntry(CO_FINAL_ORDER_FROM_DATE, data.getConditionalOrder().getPronouncedDate().plusDays(43).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }
}
