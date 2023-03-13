package uk.gov.hmcts.divorce.caseworker.service.task;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
public class GenerateFinalOrderCoverLetterTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Test
    void shouldGenerateApplicantCoverLetters() {

        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setEmail(null);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        Map<String, Object> applicant1TemplateContent = templateContent;
        applicant1TemplateContent.put(NAME, join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        applicant1TemplateContent.put(ADDRESS, caseData.getApplicant1().getPostalAddress());

        Map<String, Object> applicant2TemplateContent = templateContent;
        applicant2TemplateContent.put(NAME, join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName()));
        applicant2TemplateContent.put(ADDRESS, caseData.getApplicant2().getPostalAddress());

        generateFinalOrderCoverLetter.apply(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_1,
            applicant1TemplateContent,
            TEST_CASE_ID,
            FINAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME, now(clock))
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_2,
            applicant2TemplateContent,
            TEST_CASE_ID,
            FINAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    @Test
    void shouldNotGenerateApplicantCoverLettersIfOnline() {

        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(NO);
        caseData.getApplicant2().setOffline(NO);
        caseData.getApplicant2().setEmail("test@email.com");
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        generateFinalOrderCoverLetter.apply(details);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    public void shouldRemoveConditionalOrderGrantedDoc() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                    ListValue.<DivorceDocument>builder()
                        .id("1")
                        .value(DivorceDocument.builder()
                            .documentType(FINAL_ORDER_GRANTED_COVER_LETTER_APP_1)
                            .build())
                        .build(),
                    ListValue.<DivorceDocument>builder()
                        .id("2")
                        .value(DivorceDocument.builder()
                            .documentType(FINAL_ORDER_GRANTED_COVER_LETTER_APP_2)
                            .build())
                        .build(),
                    ListValue.<DivorceDocument>builder()
                        .id("3")
                        .value(DivorceDocument.builder()
                            .documentType(APPLICATION)
                            .build()).build()
                ))
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        generateFinalOrderCoverLetter
            .removeExistingAndGenerateNewFinalOrderGrantedCoverLetters(caseDetails);

        assertEquals(1, caseData.getDocuments().getDocumentsGenerated().size());
        assertEquals(APPLICATION, caseData.getDocuments().getDocumentsGenerated().get(0).getValue().getDocumentType());
    }
}
