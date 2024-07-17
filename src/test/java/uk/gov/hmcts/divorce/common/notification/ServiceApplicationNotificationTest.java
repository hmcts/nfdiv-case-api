package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DISPENSE_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_SOLE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_GRANTED_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SERVICE_APPLICATION_REJECTED_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVarsPreIssue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ServiceApplicationNotification serviceApplicationNotification;

    private static final YesOrNo NOT_GRANTED = YesOrNo.NO;
    private static final YesOrNo GRANTED = YesOrNo.YES;

    private static final Long ID = TEST_CASE_ID;

    @Test
    void shouldSendDeemedAsServedServiceApplicationRejectedEmailToSoleApplicantWithDivorceContent() {
        sendNotification(DEEMED, DIVORCE, NOT_GRANTED);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DEEMED_SERVICE, YES),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendDeemedAsServedServiceApplicationRejectedEmailToSoleApplicantWithDissolutionContent() {
        sendNotification(DEEMED, DISSOLUTION, NOT_GRANTED);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_DEEMED_SERVICE, YES),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendDispensedServiceApplicationRejectedEmailToSoleApplicantWithDivorceContent() {
        sendNotification(DISPENSED, DIVORCE, NOT_GRANTED);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, YES),
                hasEntry(IS_BAILIFF_SERVICE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendDispensedServiceApplicationRejectedEmailInWelsh() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getAlternativeService().setAlternativeServiceType(DISPENSED);
        data.getAlternativeService().setServiceApplicationGranted(NOT_GRANTED);

        final Map<String, String> templateVars = getMainTemplateVars();

        when(commonContent.mainTemplateVars(data, ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        serviceApplicationNotification.sendToApplicant1(data, ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, YES),
                hasEntry(IS_BAILIFF_SERVICE, NO)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendDispensedServiceApplicationRejectedEmailToSoleApplicantWithDissolutionContent() {
        sendNotification(DISPENSED, DISSOLUTION, NOT_GRANTED);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, YES),
                hasEntry(IS_BAILIFF_SERVICE, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationRejectedEmailToSoleApplicantWithDivorceContent() {
        sendNotification(BAILIFF, DIVORCE, NOT_GRANTED);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationRejectedEmailToSoleApplicantWithDissolutionContent() {
        sendNotification(BAILIFF, DISSOLUTION, NOT_GRANTED);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationSuccessfulEmailToSoleApplicant() {
        sendNotification(BAILIFF, DIVORCE, GRANTED);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_GRANTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendBailiffServiceApplicationSuccessfulEmailToSoleApplicantWhenLanguagePrefIsWelsh() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getAlternativeService().setAlternativeServiceType(BAILIFF);
        data.getAlternativeService().setServiceApplicationGranted(GRANTED);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(PARTNER, "gŵr");

        when(commonContent.mainTemplateVars(data, ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        serviceApplicationNotification.sendToApplicant1(data, ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SERVICE_APPLICATION_GRANTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, YES),
                hasEntry(PARTNER, "gŵr")
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void getEmailTemplateShouldThrowErrorIfServiceApplicationGrantedIsNull() {
        assertThatExceptionOfType(NotificationTemplateException.class)
            .isThrownBy(() -> sendNotification(BAILIFF, DIVORCE, null))
            .withMessage("Notification failed with missing field 'serviceApplicationGranted' for Case Id: 1616591401473378");
    }

    @Test
    void shouldNotThrowExceptionWhenAlternativeServiceTypeIsNull() {
        assertDoesNotThrow(
            () ->   sendNotification(null, DIVORCE, GRANTED)
        );
    }

    private void sendNotification(AlternativeServiceType alternativeServiceType, DivorceOrDissolution divorceOrDissolution,
                                  YesOrNo applicationGranted) {

        CaseData data = validApplicant1CaseData();
        data.getAlternativeService().setAlternativeServiceType(alternativeServiceType);
        data.getAlternativeService().setServiceApplicationGranted(applicationGranted);

        final Map<String, String> templateVars = getMainTemplateVars();

        if (DISSOLUTION.equals(divorceOrDissolution)) {
            templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        }

        if (applicationGranted != null) {
            when(commonContent.mainTemplateVars(data, ID, data.getApplicant1(), data.getApplicant2()))
                .thenReturn(templateVars);
        }

        serviceApplicationNotification.sendToApplicant1(data, ID);
    }

    @Test
    void shouldSendBailiffServiceApplicationSuccessfulEmailToSolicitorWhenApplicantRepresented() {
        CaseData data = validApplicant1CaseDataWithRepresentation();

        data.getAlternativeService().setAlternativeServiceType(BAILIFF);
        data.getAlternativeService().setServiceApplicationGranted(GRANTED);

        final Map<String, String> templateVars = solicitorTemplateVarsPreIssue(data, data.getApplicant1());

        when(commonContent.solicitorTemplateVarsPreIssue(data, ID, data.getApplicant1()))
            .thenReturn(templateVars);

        serviceApplicationNotification.sendToApplicant1Solicitor(data, ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SERVICE_APPLICATION_GRANTED_SOLICITOR),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DEEMED_SERVICE, NO),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, YES),
                hasEntry(IS_SOLE, YES),
                hasEntry(IS_JOINT, NO),
                hasEntry(SOLICITOR_REFERENCE, NOT_PROVIDED)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendDeemedServiceApplicationRefusedEmailToSolicitorWhenApplicantRepresented() {
        CaseData data = validApplicant1CaseDataWithRepresentation();

        data.getAlternativeService().setAlternativeServiceType(DEEMED);
        data.getAlternativeService().setServiceApplicationGranted(NOT_GRANTED);

        final Map<String, String> templateVars = solicitorTemplateVarsPreIssue(data, data.getApplicant1());

        when(commonContent.solicitorTemplateVarsPreIssue(data, ID, data.getApplicant1()))
            .thenReturn(templateVars);

        serviceApplicationNotification.sendToApplicant1Solicitor(data, ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(SERVICE_APPLICATION_REJECTED_SOLICITOR),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(ID)),
                hasEntry(IS_DEEMED_SERVICE, YES),
                hasEntry(IS_DISPENSE_SERVICE, NO),
                hasEntry(IS_BAILIFF_SERVICE, NO),
                hasEntry(IS_SOLE, YES),
                hasEntry(IS_JOINT, NO),
                hasEntry(SOLICITOR_REFERENCE, NOT_PROVIDED)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    CaseData validApplicant1CaseDataWithRepresentation() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        return data;
    }
}
