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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOREIGN_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NAME_CHANGE_PROOF;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS_SERVED_ANOTHER_WAY_APPLY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS_SERVED_ANOTHER_WAY_PARAGRAPH;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS_SERVED_ANOTHER_WAY_TITLE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
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

        Set<DocumentType> docs = new HashSet<>();
        docs.add(DocumentType.MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);

        final HashMap<String, String> templateVars = new HashMap<>();
        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(FOREIGN_CERTIFICATE, "* Your original foreign marriage certificate"),
                hasEntry(FOREIGN_CERTIFICATE_TRANSLATION, "* A certified translation of your foreign marriage certificate"),
                hasEntry(NAME_CHANGE_PROOF, "* Proof that you changed your name. For example deed poll or statutory declaration")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldCallSendEmailToApplicant2ForSupportingDocuments() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.NO);
        data.getApplication().setApplicant2CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));

        final HashMap<String, String> templateVars = new HashMap<>();
        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(NAME_CHANGE_PROOF, "* Proof that you changed your name. For example deed poll or statutory declaration")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    @SuppressWarnings("squid:S6068")
    void shouldCallSendEmailForPapersServedAnotherWay() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant2(getApplicant2(Gender.MALE));
        data.getApplication().getMarriageDetails().setMarriedInUk(YesOrNo.YES);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(DocumentType.MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(docs);

        final HashMap<String, String> templateVars = new HashMap<>();
        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant2())).thenReturn("husband");
        when(commonContent.getService(data.getDivorceOrDissolution())).thenReturn("divorce");

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(CERTIFICATE, "* Your original marriage certificate or a certified copy"),
                hasEntry(NAME_CHANGE_PROOF, "* Proof that you changed your name. For example deed poll or statutory declaration"),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY_TITLE, "# Apply to serve the divorce papers another way"),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY_PARAGRAPH,
                    "You need to apply to serve the divorce papers to your husband another way. This is because you did not provide their"
                        + " postal address in the application. For example you could try to serve them by email,"
                        + " text message or social media."),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY_APPLY,
                    "You can apply here: https://www.gov.uk/government/publications/form-d11-application-notice"),
                hasEntry(FOREIGN_CERTIFICATE, ""),
                hasEntry(FOREIGN_CERTIFICATE_TRANSLATION, "")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
        verify(commonContent).getTheirPartner(data, data.getApplicant2());
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

        final HashMap<String, String> templateVars = new HashMap<>();
        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(OUTSTANDING_ACTIONS),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(CERTIFICATE, "* Your original civil partnership certificate or a certified copy"),
                hasEntry(NAME_CHANGE_PROOF, "* Proof that you changed your name. For example deed poll or statutory declaration"),
                hasEntry(PAPERS_SERVED_ANOTHER_WAY_TITLE, "# Apply to serve the papers another way")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

}
