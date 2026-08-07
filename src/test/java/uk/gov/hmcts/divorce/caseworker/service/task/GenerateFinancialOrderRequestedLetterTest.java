package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_FO_REQUESTED_LETTER_RESPONDENT_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_FO_REQUESTED_LETTER_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINANCIAL_ORDER_REQUESTED_LETTER_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;


@ExtendWith(MockitoExtension.class)
class GenerateFinancialOrderRequestedLetterTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;
    @Mock
    private Clock clock;
    @Mock
    private DocmosisCommonContent docmosisCommonContent;
    @Mock
    private PaymentService paymentService;
    @Mock
    private CommonContent commonContent;
    @Mock
    private DocmosisTemplatesConfig config;

    @InjectMocks
    private GenerateFinancialOrderRequestedLetter generateFinancialOrderRequestedLetter;

    @Test
    void shouldNotGenerateFinancialOrderRequestedLetterForJointCases() {
        CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);

        generateFinancialOrderRequestedLetter.apply(caseData);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    void shouldNotGenerateFinancialOrderRequestedLetterWhenFOIsNotRequested() {
        CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplication().setServiceMethod(ServiceMethod.PERSONAL_SERVICE);

        generateFinancialOrderRequestedLetter.apply(caseData);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    void shouldNotGenerateFinancialOrderRequestedLetterWhenRespondentIsRepresented() {
        CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.PERSONAL_SERVICE);

        generateFinancialOrderRequestedLetter.apply(caseData);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    void shouldNotGenerateFinancialOrderRequestedWhenCourtService() {
        CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplication().setServiceMethod(ServiceMethod.COURT_SERVICE);

        generateFinancialOrderRequestedLetter.apply(caseData);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    void shouldCallCaseDataDocumentServiceToGenerateLetter() {
        setMockClock(clock);
        CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplication().setServiceMethod(ServiceMethod.PERSONAL_SERVICE);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(paymentService.getServiceCost(anyString(), anyString(), anyString())).thenReturn(275.0);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(new HashMap<>());
        when(commonContent.getPartner(caseData, caseData.getApplicant1())).thenReturn("wife");
        when(config.getTemplateVars()).thenReturn(Map.of("welshEnquiriesEmail", "test@test,com"));

        Map<String, Object> templateContent = getTestTemplateContent(caseData);

        generateFinancialOrderRequestedLetter.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                FINANCIAL_ORDER_REQUESTED_LETTER_RESPONDENT,
                templateContent,
                TEST_CASE_ID,
                NFD_FO_REQUESTED_LETTER_RESPONDENT_TEMPLATE_ID,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, NFD_FO_REQUESTED_LETTER_RESPONDENT_DOCUMENT_NAME, now(clock))
            );
    }

    private Map<String, Object> getTestTemplateContent(CaseData caseData) {
        final String applicant2Address = caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck();
        Map<String, Object> templateContent = new HashMap<>();
        templateContent.put("caseReference", "1616-5914-0147-3378");
        templateContent.put("firstName", "test_first_name");
        templateContent.put("lastName", "test_last_name");
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put("isDivorce", true);
        templateContent.put("address", applicant2Address);
        templateContent.put("financialOrderFees", "£275.00");
        templateContent.put("consentOrderFees", "£275.00");
        templateContent.put("partner", "wife");
        templateContent.put("welshEnquiriesEmail", "test@test,com");
        return templateContent;
    }
}
