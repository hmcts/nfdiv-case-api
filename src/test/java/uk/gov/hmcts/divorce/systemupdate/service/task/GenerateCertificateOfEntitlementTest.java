package uk.gov.hmcts.divorce.systemupdate.service.task;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CertificateOfEntitlementContent;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement.GET_A_DIVORCE;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement.IS_JOINT;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement.IS_RESPONDENT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getSolicitorDocTemplateContent;

@ExtendWith(MockitoExtension.class)
class GenerateCertificateOfEntitlementTest {

    private static final String TEST_ADDRESS = "line1\nline2\ncity\npostcode";

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CertificateOfEntitlementContent certificateOfEntitlementContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(generateCertificateOfEntitlement, "finalOrderOffsetDays", 43);
    }

    @Test
    void shouldGenerateCertificateOfEntitlementAndUpdateCaseData() {

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        final Document document = Document.builder()
            .filename("filename")
            .build();

        setMockClock(clock);
        when(certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME, LocalDateTime.now(clock))))
            .thenReturn(document);

        final CaseDetails<CaseData, State> result = generateCertificateOfEntitlement.apply(caseDetails);

        final DivorceDocument certificateOfEntitlementDocument = result.getData()
            .getConditionalOrder()
            .getCertificateOfEntitlementDocument();

        assertThat(certificateOfEntitlementDocument.getDocumentLink()).isSameAs(document);
        assertThat(certificateOfEntitlementDocument.getDocumentFileName()).isEqualTo("filename");
        assertThat(certificateOfEntitlementDocument.getDocumentType()).isEqualTo(CERTIFICATE_OF_ENTITLEMENT);
    }

    @Test
    void shouldGenerateCertificateOfEntitlementAndUpdateCaseDataForJudicialSeparation() {

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        final Document document = Document.builder()
            .filename("filename")
            .build();

        setMockClock(clock);
        when(certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        when(caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME, LocalDateTime.now(clock))))
            .thenReturn(document);

        final CaseDetails<CaseData, State> result = generateCertificateOfEntitlement.apply(caseDetails);

        final DivorceDocument certificateOfEntitlementDocument = result.getData()
            .getConditionalOrder()
            .getCertificateOfEntitlementDocument();

        assertThat(certificateOfEntitlementDocument.getDocumentLink()).isSameAs(document);
        assertThat(certificateOfEntitlementDocument.getDocumentFileName()).isEqualTo("filename");
        assertThat(certificateOfEntitlementDocument.getDocumentType()).isEqualTo(CERTIFICATE_OF_ENTITLEMENT);


    }

    @Test
    void shouldRenderCoverLetterAddressedToApplicant1IfNotRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getApplicant2().setOffline(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> applicant1TemplateVars = new HashMap<>();
        applicant1TemplateVars.put(NAME, "Bob Smith");
        applicant1TemplateVars.put(ADDRESS, TEST_ADDRESS);
        applicant1TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant1TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant1TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant1TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant1TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant1TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant1TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant1TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant1TemplateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        applicant1TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            applicant1TemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterAddressedToApplicant1SolicitorIfRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .address("5 The Street,\n London,\n W1 1BW")
                .build()
        );
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setOffline(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "App1 Sol");
        templateVars.put(ADDRESS, "5 The Street,\n London,\n W1 1BW");
        templateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        templateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        templateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        templateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        templateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        templateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        templateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        templateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        templateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderJointCoverLetterForApplicant2() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setOffline(NO);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(NAME, "Julie Smith");
        applicant2TemplateVars.put(ADDRESS, TEST_ADDRESS);
        applicant2TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant2TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant2TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant2TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        applicant2TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant2TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant2TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant2TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterAddressedToApplicant2IfNotRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(NO);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(NAME, "Julie Smith");
        applicant2TemplateVars.put(ADDRESS, TEST_ADDRESS);
        applicant2TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        applicant2TemplateVars.put(PARTNER, "husband");

        applicant2TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant2TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant2TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        applicant2TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant2TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant2TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant2TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterAddressedToApplicant2SolicitorIfRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(NO);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .address("5 The Street,\n London,\n W1 1BW")
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "App2 Sol");
        templateVars.put(ADDRESS, "5 The Street,\n London,\n W1 1BW");
        templateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(PARTNER, "husband");

        templateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        templateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        templateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        templateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        templateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        templateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        templateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        templateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        templateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("husband");

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    public void shouldRemoveCertificateOfEntitlementCoverLettersDoc() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                    ListValue.<DivorceDocument>builder()
                        .id("1")
                        .value(DivorceDocument.builder()
                            .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1)
                            .build())
                        .build(),
                    ListValue.<DivorceDocument>builder()
                        .id("2")
                        .value(DivorceDocument.builder()
                            .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2)
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

        generateCertificateOfEntitlement
            .removeExistingAndGenerateNewCertificateOfEntitlementCoverLetters(caseDetails);

        assertEquals(1, caseData.getDocuments().getDocumentsGenerated().size());
        assertEquals(APPLICATION, caseData.getDocuments().getDocumentsGenerated().get(0).getValue().getDocumentType());
    }

    @Test
    void shouldRenderCoverLetterAddressedToApplicantAndRespondentWhenSoleJudicialSeparationAndBothApplicantsOffline() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> applicantTemplateVars = new HashMap<>();
        applicantTemplateVars.put(NAME, "Bob Smith");
        applicantTemplateVars.put(ADDRESS, TEST_ADDRESS);
        applicantTemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicantTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicantTemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicantTemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicantTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicantTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicantTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicantTemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicantTemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicantTemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicantTemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicantTemplateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        applicantTemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicantTemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        applicantTemplateVars.put(IS_DIVORCE, true);
        applicantTemplateVars.put(IS_JOINT, false);

        Map<String, Object> respondentTemplateVars = new HashMap<>();
        respondentTemplateVars.put(NAME, "Julie Smith");
        respondentTemplateVars.put(ADDRESS, TEST_ADDRESS);
        respondentTemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        respondentTemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        respondentTemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        respondentTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        respondentTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        respondentTemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        respondentTemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        respondentTemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        respondentTemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        respondentTemplateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        respondentTemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(IS_RESPONDENT, true);
        respondentTemplateVars.put(IS_DIVORCE, true);
        respondentTemplateVars.put(IS_JOINT, false);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            applicantTemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            respondentTemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterAddressedToBothApplicantsWhenJointJudicialSeparationAndBothApplicantsOffline() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> applicant1TemplateVars = new HashMap<>();
        applicant1TemplateVars.put(NAME, "Bob Smith");
        applicant1TemplateVars.put(ADDRESS, TEST_ADDRESS);
        applicant1TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant1TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant1TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant1TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant1TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant1TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant1TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant1TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant1TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(IS_JOINT, true);

        Map<String, Object> applicant2TemplateVars = new HashMap<>();
        applicant2TemplateVars.put(NAME, "Julie Smith");
        applicant2TemplateVars.put(ADDRESS, TEST_ADDRESS);
        applicant2TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant2TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant2TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant2TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicant2TemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicant2TemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicant2TemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicant2TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(IS_RESPONDENT, false);
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(IS_JOINT, true);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            applicant1TemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            applicant2TemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterAddressedToSolicitorsWhenSoleJudicialSeparationAndBothApplicantsOfflineAndRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseDataWithSolicitor();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> applicantTemplateVars = new HashMap<>();
        applicantTemplateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        applicantTemplateVars.put(SOLICITOR_REFERENCE, caseData.getApplicant1().getSolicitor().getReference());
        applicantTemplateVars.put(SOLICITOR_ADDRESS, caseData.getApplicant1().getSolicitor().getAddress());
        applicantTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicantTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicantTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicantTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicantTemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicantTemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicantTemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicantTemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicantTemplateVars.put(IS_DIVORCE, true);
        applicantTemplateVars.put(IS_JOINT, false);

        Map<String, Object> respondentTemplateVars = new HashMap<>(applicantTemplateVars);
        respondentTemplateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        respondentTemplateVars.put(SOLICITOR_REFERENCE, caseData.getApplicant2().getSolicitor().getReference());
        respondentTemplateVars.put(SOLICITOR_ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, ENGLISH))
            .thenReturn(getSolicitorDocTemplateContent(caseData, caseData.getApplicant1()));

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getSolicitorDocTemplateContent(caseData, caseData.getApplicant2()));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            applicantTemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            respondentTemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterAddressedToApplicantsSolicitorWhenJointJudicialSeparationAndBothApplicantsOfflineAndRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseDataWithSolicitor();
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> applicantTemplateVars = new HashMap<>();
        applicantTemplateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        applicantTemplateVars.put(SOLICITOR_REFERENCE, caseData.getApplicant1().getSolicitor().getReference());
        applicantTemplateVars.put(SOLICITOR_ADDRESS, caseData.getApplicant1().getSolicitor().getAddress());
        applicantTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicantTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicantTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicantTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicantTemplateVars.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        applicantTemplateVars.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        applicantTemplateVars.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        applicantTemplateVars.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        applicantTemplateVars.put(IS_DIVORCE, true);
        applicantTemplateVars.put(IS_JOINT, true);

        Map<String, Object> respondentTemplateVars = new HashMap<>(applicantTemplateVars);
        respondentTemplateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        respondentTemplateVars.put(SOLICITOR_REFERENCE, caseData.getApplicant2().getSolicitor().getReference());
        respondentTemplateVars.put(SOLICITOR_ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, ENGLISH))
            .thenReturn(getSolicitorDocTemplateContent(caseData, caseData.getApplicant1()));

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getSolicitorDocTemplateContent(caseData, caseData.getApplicant2()));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            applicantTemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            respondentTemplateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private CaseData caseDataWithSolicitor() {
        final CaseData caseData = caseData();
        caseData.setIsJudicialSeparation(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        String sol1Name = "Sol1";
        String sol2Name = "Sol2";
        String sol1Reference = "1234";
        String sol2Reference = "4567";

        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .reference(sol1Reference)
            .name(sol1Name)
            .address(TEST_ADDRESS)
            .build());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .reference(sol2Reference)
            .name(sol2Name)
            .address(TEST_ADDRESS)
            .build());

        return caseData;
    }

    private CaseData caseData() {

        return CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .offline(YES)
                    .address(APPLICANT_ADDRESS)
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(
                Applicant.builder()
                    .firstName("Julie")
                    .lastName("Smith")
                    .offline(YES)
                    .address(APPLICANT_ADDRESS)
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .languagePreferenceWelsh(NO)
                    .build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.of(2022, 4, 28, 10, 0, 0))
                    .court(BURY_ST_EDMUNDS)
                    .build()
            )
            .build();
    }

    private CtscContactDetails buildCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .emailAddress("contactdivorce@justice.gov.uk")
            .build();
    }
}
