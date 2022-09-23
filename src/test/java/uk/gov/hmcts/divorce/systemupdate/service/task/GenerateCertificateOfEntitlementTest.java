package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement.GET_A_DIVORCE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class GenerateCertificateOfEntitlementTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CertificateOfEntitlementContent certificateOfEntitlementContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

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
    void shouldRenderCoverLetterAddressedToApplicantIfNotRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(NAME, "Bob Smith");
        templateVars.put(ADDRESS, "line1\nline2\ncity\npostcode");
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
        templateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        templateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));


        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetter(caseData, TEST_CASE_ID, caseData.getApplicant1());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    @Test
    void shouldRenderCoverLetterAddressedToApplicantSolicitorIfRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .address("5 The Street,\n London,\n W1 1BW")
                .build()
        );
        caseData.getApplicant1().setSolicitorRepresented(YES);

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
        templateVars.put(CTSC_CONTACT_DETAILS, buildCtscContactDetails());
        templateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        generateCertificateOfEntitlement.generateCertificateOfEntitlementCoverLetter(caseData, TEST_CASE_ID, caseData.getApplicant1());

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER,
            templateVars,
            TEST_CASE_ID,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
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
            .emailAddress("divorcecase@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .build();
    }
}
