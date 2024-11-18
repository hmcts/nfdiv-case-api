package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ChangedNameHow;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.CONDITIONAL_COURT_EMAIL;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.CONDITIONAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_FOREIGN_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.MISSING_NAME_CHANGE_PROOF;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.PAPERS_SERVED_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.SEND_DOCUMENTS_TO_COURT;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.SEND_DOCUMENTS_TO_COURT_DISSOLUTION;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.SEND_DOCUMENTS_TO_COURT_DIVORCE;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification.SERVE_HUSBAND_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ChangedNameHow.DEED_POLL;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
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
    void shouldCallSendEmailToApplicant1ForSupportingDocuments() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.setApplicationType(SOLE_APPLICATION);
        Set<DocumentType> docs = new HashSet<>();
        docs.add(MARRIAGE_CERTIFICATE);
        docs.add(MARRIAGE_CERTIFICATE_TRANSLATION);
        docs.add(NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        data.getApplicant1().setNameChangedHow(Set.of(DEED_POLL));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, FORMATTED_TEST_CASE_ID),
                hasEntry(CONDITIONAL_COURT_EMAIL, "courtEmail"),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, YES),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, YES),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailInWelshToApplicant1ForSupportingDocuments() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(MALE));
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.setApplicationType(SOLE_APPLICATION);
        Set<DocumentType> docs = new HashSet<>();
        docs.add(MARRIAGE_CERTIFICATE);
        docs.add(MARRIAGE_CERTIFICATE_TRANSLATION);
        docs.add(NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        data.getApplicant1().setNameChangedHow(Set.of(DEED_POLL));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, FORMATTED_TEST_CASE_ID),
                hasEntry(CONDITIONAL_COURT_EMAIL, "courtEmail"),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, YES),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, YES),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldNotCallSendEmailToApplicant1IfNoAwaitingDocuments() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldCallSendEmailToApplicant2ForSupportingDocuments() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().setApplicant2CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setNameChangedHow(Set.of(DEED_POLL));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, FORMATTED_TEST_CASE_ID),
                hasEntry(CONDITIONAL_COURT_EMAIL, "courtEmail"),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailInWelshToApplicant2ForSupportingDocuments() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().setApplicant2CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.getApplicant2().setNameChangedHow(Set.of(DEED_POLL));
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getApplicant2().setEmail(null);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, FORMATTED_TEST_CASE_ID),
                hasEntry(CONDITIONAL_COURT_EMAIL, "courtEmail"),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailToApplicant2IfApplicant1IsMissingDocuments() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.getApplicant1().setNameChangedHow(Set.of(DEED_POLL));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, FORMATTED_TEST_CASE_ID),
                hasEntry(CONDITIONAL_COURT_EMAIL, "courtEmail"),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldNotSendEmailToApplicant2IfSoleApplication() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().setApplicant2CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotCallSendEmailToApplicant2IfNoAwaitingDocuments() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldCallSendEmailForPapersServedAnotherWay() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant2(getApplicant2(MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);
        data.setApplicationType(SOLE_APPLICATION);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        data.getApplicant1().setNameChangedHow(Set.of(DEED_POLL));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, FORMATTED_TEST_CASE_ID),
                hasEntry(CONDITIONAL_COURT_EMAIL, "courtEmail"),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE, YES),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY, YES),
                hasEntry(SERVE_HUSBAND_ANOTHER_WAY, YES),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, CommonContent.NO),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, CommonContent.NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailForCivil() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        data.getApplicant1().setNameChangedHow(Set.of(DEED_POLL));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, FORMATTED_TEST_CASE_ID),
                hasEntry(CONDITIONAL_COURT_EMAIL, "courtEmail"),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE, YES),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailForPapersServedAnotherWayAndNoDocumentsRequired() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant2(getApplicant2(MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);
        data.setApplicationType(SOLE_APPLICATION);

        data.getApplication().setApplicant1CannotUploadSupportingDocument(new HashSet<>());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(SEND_DOCUMENTS_TO_COURT, CommonContent.NO),
                hasEntry(CONDITIONAL_REFERENCE_NUMBER, ""),
                hasEntry(CONDITIONAL_COURT_EMAIL, ""),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE, CommonContent.NO),
                hasEntry(MISSING_NAME_CHANGE_PROOF, CommonContent.NO),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY, YES),
                hasEntry(SERVE_HUSBAND_ANOTHER_WAY, YES),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, CommonContent.NO),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, CommonContent.NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void assertSendDocumentsToCourtDissolution() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DISSOLUTION);

        data.setApplicant2(getApplicant2(MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);
        data.setApplicationType(SOLE_APPLICATION);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(SEND_DOCUMENTS_TO_COURT, YES),
                hasEntry(SEND_DOCUMENTS_TO_COURT_DIVORCE, NO),
                hasEntry(SEND_DOCUMENTS_TO_COURT_DISSOLUTION, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void assertSendDocumentsToCourtDissolutionWhenNotSendingDocumentsToCourt() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DISSOLUTION);

        data.setApplicant2(getApplicant2(MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);
        data.setApplicationType(SOLE_APPLICATION);

        data.getApplication().setApplicant1CannotUploadSupportingDocument(new HashSet<>());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(SEND_DOCUMENTS_TO_COURT, NO),
                hasEntry(SEND_DOCUMENTS_TO_COURT_DIVORCE, NO),
                hasEntry(SEND_DOCUMENTS_TO_COURT_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void assertSendDocumentsToCourtDissolutionWhenNotSendingDocumentsToCour() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DISSOLUTION);

        data.setApplicant2(getApplicant2(MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);
        data.setApplicationType(SOLE_APPLICATION);

        data.getApplication().setApplicant1CannotUploadSupportingDocument(new HashSet<>());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(SEND_DOCUMENTS_TO_COURT, NO),
                hasEntry(SEND_DOCUMENTS_TO_COURT_DIVORCE, NO),
                hasEntry(SEND_DOCUMENTS_TO_COURT_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void assertContentWhenMissingNameChangeEvidenceIsMarriageCertificate() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DIVORCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplicant1().setNameChangedHow(Set.of(ChangedNameHow.MARRIAGE_CERTIFICATE));
        data.setApplicationType(JOINT_APPLICATION);
        data.setApplicant2(getApplicant2(MALE));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(MISSING_MARRIAGE_CERTIFICATE, YES),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE, NO),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, NO),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION, NO),
                hasEntry(MISSING_NAME_CHANGE_PROOF, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void assertContentWhenMissingNameChangeEvidenceIsDeedPoll() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplicant1().setNameChangedHow(Set.of(DEED_POLL));
        data.setApplicationType(JOINT_APPLICATION);
        data.setApplicant2(getApplicant2(MALE));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(MISSING_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE, NO),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, NO),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION, NO),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void assertContentWhenMissingNameChangeEvidenceIsMarriageCertificateTranslated() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DIVORCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().getMarriageDetails().setCertifiedTranslation(YesOrNo.YES);
        data.getApplicant1().setNameChangedHow(Set.of(ChangedNameHow.MARRIAGE_CERTIFICATE));
        data.setApplicationType(JOINT_APPLICATION);
        data.setApplicant2(getApplicant2(MALE));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(MISSING_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE, NO),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, YES),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION, NO),
                hasEntry(MISSING_NAME_CHANGE_PROOF, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void assertContentWhenMissingMarriageCertificateAndDeedPollForeignCivilPartnership() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE, MARRIAGE_CERTIFICATE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().getMarriageDetails().setCertifiedTranslation(YesOrNo.NO);
        data.getApplicant1().setNameChangedHow(Set.of(DEED_POLL));
        data.setApplicationType(JOINT_APPLICATION);
        data.setApplicant2(getApplicant2(MALE));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(MISSING_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_MARRIAGE_CERTIFICATE, NO),
                hasEntry(MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE, YES),
                hasEntry(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION, NO),
                hasEntry(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION, NO),
                hasEntry(MISSING_NAME_CHANGE_PROOF, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
