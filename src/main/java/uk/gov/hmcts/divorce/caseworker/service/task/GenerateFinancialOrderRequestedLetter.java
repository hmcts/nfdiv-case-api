package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_FO_REQUESTED_LETTER_RESPONDENT_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_FO_REQUESTED_LETTER_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WELSH_ENQUIRIES_EMAIL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINANCIAL_ORDER_REQUESTED_LETTER_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.FEES_CONSENT_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.FEES_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_MISC;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_CONSENT_ORDER;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_FINANCIAL_ORDER_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenerateFinancialOrderRequestedLetter implements CaseTask {
    private final CaseDataDocumentService caseDataDocumentService;
    private final Clock clock;
    private final DocmosisCommonContent docmosisCommonContent;
    private final PaymentService paymentService;
    private final CommonContent commonContent;
    private final DocmosisTemplatesConfig config;

    public void apply(final CaseData caseData) {
        apply(caseData, null);
    }

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        apply(caseDetails.getData(), caseDetails.getId());
        return caseDetails;
    }

    private void apply(final CaseData caseData, final Long caseId) {
        removeExistingFORequestedLetter(caseData, caseId);
        if (shouldGenerateFinancialOrderLetter(caseData)) {
            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseData,
                FINANCIAL_ORDER_REQUESTED_LETTER_RESPONDENT,
                getTemplateContent(caseData, caseId),
                caseId,
                NFD_FO_REQUESTED_LETTER_RESPONDENT_TEMPLATE_ID,
                LanguagePreference.ENGLISH,
                formatDocumentName(caseId, NFD_FO_REQUESTED_LETTER_RESPONDENT_DOCUMENT_NAME, now(clock))
            );
        }
    }

    private Map<String, Object> getTemplateContent(CaseData caseData, Long caseId) {
        Applicant applicant = caseData.getApplicant2();
        Map<String, Object> templateContent =
            docmosisCommonContent.getBasicDocmosisTemplateContent(LanguagePreference.ENGLISH);
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());

        String financialOrderFees = formatAmount(paymentService.getServiceCost(SERVICE_OTHER,
            EVENT_MISC,
            KEYWORD_FINANCIAL_ORDER_NOTICE));
        templateContent.put(FEES_FINANCIAL_ORDER, financialOrderFees);

        String consentOrderFees = formatAmount(paymentService.getServiceCost(SERVICE_OTHER,
            EVENT_GENERAL,
            KEYWORD_CONSENT_ORDER));
        templateContent.put(FEES_CONSENT_ORDER, consentOrderFees);
        templateContent.put(PARTNER, commonContent.getPartner(caseData, caseData.getApplicant1()));
        templateContent.put(WELSH_ENQUIRIES_EMAIL, config.getTemplateVars().get(WELSH_ENQUIRIES_EMAIL));

        return templateContent;
    }

    private void removeExistingFORequestedLetter(final CaseData caseData, Long caseId) {
        if (documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), FINANCIAL_ORDER_REQUESTED_LETTER_RESPONDENT)) {
            log.info("Removing existing financial order requested letter for case id: {}", caseId);
            caseData.getDocuments().setDocumentsGenerated(
                caseData.getDocuments().getDocumentsGenerated()
                    .stream()
                    .filter(doc -> !FINANCIAL_ORDER_REQUESTED_LETTER_RESPONDENT.equals(doc.getValue().getDocumentType()))
                    .toList()
            );
        }
    }

    public static boolean shouldGenerateFinancialOrderLetter(final CaseData caseData) {
        boolean isSole = caseData.getApplicationType().isSole();
        boolean hasFOBeenRequested = YesOrNo.YES.equals(caseData.getApplicant1().getFinancialOrder());
        boolean isRepresented = caseData.getApplicant2().isRepresented();
        boolean isCourtServiceMethod = caseData.getApplication().isCourtServiceMethod();

        return isSole && !isRepresented && !isCourtServiceMethod && hasFOBeenRequested;
    }
}
