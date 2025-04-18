package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.notification.NocSolRemovedCitizenNotification.LETTER_TYPE_NOTIFY_CITIZEN_SOL_STOPPED_REP;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_SOL_STOPPED_REP_NOTIFY_APP_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_SOL_STOPPED_REP_NOTIFY_APP_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
class NocSolRemovedCitizenNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @InjectMocks
    private NocSolRemovedCitizenNotification notificationHandler;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private EmailTemplatesConfig config;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    private Map<String, String> getTemplateVars(Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(SMART_SURVEY, SMART_SURVEY);
        return templateVars;
    }

    @Test
    void shouldSendOnlyLetterToApplicantWhenEmailForApplicantIsBlank() {
        CaseData caseData = createMockCaseData();
        caseData.getApplicant1().setEmail(null);

        final Map<String, Object> templateContent = getBasicDocmosisTemplateContent(ENGLISH);

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(templateContent);

        Document inviteDocument =
            Document.builder()
                .url("testUrl")
                .filename("testFileName")
                .binaryUrl("binaryUrl")
                .build();

        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            NFD_SOL_STOPPED_REP_NOTIFY_APP_TEMPLATE_ID,
            ENGLISH,
            NFD_SOL_STOPPED_REP_NOTIFY_APP_DOCUMENT_NAME))
            .thenReturn(inviteDocument);

        notificationHandler.send(caseData, true, TEST_CASE_ID);

        verifyNoInteractions(notificationService);

        final Print print = printCaptor.getValue();

        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_NOTIFY_CITIZEN_SOL_STOPPED_REP);
        assertThat(print.getLetters()).hasSize(1);
        assertThat(print.getLetters().get(0).getDocument()).isSameAs(inviteDocument);
        verify(caseDataDocumentService)
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                NFD_SOL_STOPPED_REP_NOTIFY_APP_TEMPLATE_ID,
                ENGLISH, NFD_SOL_STOPPED_REP_NOTIFY_APP_DOCUMENT_NAME);
    }

    @Test
    void shouldSendEmailToApplicantWhenEmailForApplicantIsNotBlank() {
        CaseData caseData = createMockCaseData();

        final Map<String, Object> templateContent = getBasicDocmosisTemplateContent(ENGLISH);

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()
        )).thenReturn(getTemplateVars(caseData.getApplicant1()));

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(templateContent);

        Document inviteDocument =
            Document.builder()
                .url("testUrl")
                .filename("testFileName")
                .binaryUrl("binaryUrl")
                .build();

        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            NFD_SOL_STOPPED_REP_NOTIFY_APP_TEMPLATE_ID,
            ENGLISH,
            NFD_SOL_STOPPED_REP_NOTIFY_APP_DOCUMENT_NAME))
            .thenReturn(inviteDocument);

        notificationHandler.send(caseData, true, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(EmailTemplateName.SOLICITOR_STOP_REPRESENTATION_SELF_NOTIFY_CITIZEN),
            argThat(allOf(
                hasEntry(SMART_SURVEY, SMART_SURVEY)
            )),
            eq(caseData.getApplicant1().getLanguagePreference()),
            eq(TEST_CASE_ID)
        );
    }

    private CaseData createMockCaseData() {
        CaseData caseData = CaseData.builder().build();
        caseData.setApplicant1(createApplicant(TEST_USER_EMAIL, YesOrNo.NO));
        caseData.setApplicant2(createApplicant(TEST_USER_EMAIL, YesOrNo.YES));
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        return caseData;
    }

    private Applicant createApplicant(String email, YesOrNo languagePreferenceWelsh) {
        return Applicant.builder()
            .email(email)
            .languagePreferenceWelsh(languagePreferenceWelsh)
            .build();
    }
}
