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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CertificateOfEntitlementContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
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
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2WithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getOfflineSolicitor;

@ExtendWith(MockitoExtension.class)
class GenerateCertificateOfEntitlementTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CertificateOfEntitlementContent certificateOfEntitlementContent;

    @Mock
    private GenerateCertificateOfEntitlementHelper generateCertificateOfEntitlementHelper;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(generateCertificateOfEntitlementHelper, "finalOrderOffsetDays", 43);
    }

    @Test
    void shouldGenerateCertificateOfEntitlementAndUpdateCaseData() {

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = getCaseData();
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(caseData);
        final Document document = Document.builder()
            .filename("filename")
            .build();

        setMockClock(clock);

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
        final CaseData caseData = getCaseData();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(caseData);
        final Document document = Document.builder()
            .filename("filename")
            .build();

        setMockClock(clock);

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
    void shouldRenderCoverLetterForApplicant1() {

        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.getApplicant2().setOffline(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(caseData);

        Map<String, Object> templateVars = new HashMap<>();

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(caseDetails);

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

        final CaseData caseData = getCaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setOffline(NO);
        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(caseData);

        Map<String, Object> templateVars = new HashMap<>();

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    public void shouldRemoveCertificateOfEntitlementCoverLettersDoc() {

        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.setDocuments(CaseDocuments.builder()
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
            .build());

        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(caseData);

        generateCertificateOfEntitlement
            .removeExistingAndGenerateNewCertificateOfEntitlementCoverLetters(caseDetails);

        assertEquals(1, caseData.getDocuments().getDocumentsGenerated().size());
        assertEquals(APPLICATION, caseData.getDocuments().getDocumentsGenerated().get(0).getValue().getDocumentType());
    }

    @Test
    void shouldRenderCoverLetterForApplicantAndRespondentWhenSoleJudicialSeparationAndBothApplicantsOffline() {

        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(caseData);

        Map<String, Object> templateVars = new HashMap<>();

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterForBothApplicantsWhenJointJudicialSeparationAndBothApplicantsOffline() {

        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        final CaseDetails<CaseData, State> caseDetails = getCaseDetails(caseData);

        Map<String, Object> templateVars = new HashMap<>();

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterForSolicitorsWhenSoleJudicialSeparationAndBothApplicantsOfflineAndRepresented() {

        setMockClock(clock);

        final CaseData caseData = getCaseDataWithSolicitor();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        final CaseDetails<CaseData, State> details = getCaseDetails(caseData);

        Map<String, Object> templateVars = new HashMap<>();

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterForApplicantsSolicitorWhenJointJudicialSeparationAndBothApplicantsOfflineAndRepresented() {

        setMockClock(clock);

        final CaseData caseData = getCaseDataWithSolicitor();
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        Map<String, Object> templateVars = new HashMap<>();

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetters(details);

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );

        verify(caseDataDocumentService, atMostOnce()).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private CaseData getCaseDataWithSolicitor() {
        final CaseData caseData = getCaseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant2().getSolicitor().setReference(TEST_REFERENCE);

        return caseData;
    }

    private CaseData getCaseData() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getApplicant1().setEmail(null);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.setApplicant2(getApplicant2WithAddress());
        caseData.getApplicant2().setEmail(null);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2022, 4, 28, 10, 0, 0))
            .court(BURY_ST_EDMUNDS)
            .build());

        return caseData;
    }

    private CaseDetails<CaseData, State> getCaseDetails(final CaseData caseData) {
        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        return details;
    }
}
