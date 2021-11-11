package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_FOREIGN_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_NAME_CHANGE_PROOF;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.PAPERS_SERVED_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.SERVE_HUSBAND_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getCommonTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class ApplicationOutstandingActionNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationOutstandingActionNotification notification;

    @Test
    @SuppressWarnings("squid:S6068")
    void shouldCallSendEmailToApplicant1ForSupportingDocuments() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(Gender.MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.setApplicationType(SOLE_APPLICATION);
        Set<DocumentType> docs = new HashSet<>();
        docs.add(DocumentType.MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION);
        docs.add(NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getCommonTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, YES),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, YES),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldCallSendEmailToApplicant2ForSupportingDocuments() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().setApplicant2CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getCommonTemplateVars());

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    @SuppressWarnings("squid:S6068")
    void shouldCallSendEmailForPapersServedAnotherWay() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant2(getApplicant2(Gender.MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);
        data.setApplicationType(SOLE_APPLICATION);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(DocumentType.MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getCommonTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE, YES),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY, YES),
                hasEntry(SERVE_HUSBAND_ANOTHER_WAY, YES),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    @SuppressWarnings("squid:S6068")
    void shouldCallSendEmailForCivil() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(DocumentType.MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getCommonTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE, YES),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY, YES)
            )),
            eq(ENGLISH)
        );
    }
}
