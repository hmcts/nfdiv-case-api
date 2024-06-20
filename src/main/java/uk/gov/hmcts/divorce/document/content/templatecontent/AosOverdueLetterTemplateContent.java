package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_JS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_OVERDUE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ARRANGE_SERVICE_BY_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BAILIFF_SERVICE_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CP_CASE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DEEMED_SERVICE_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISPENSED_SERVICE_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SEARCH_ADDRESS_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVE_BY_EMAIL_COST;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@RequiredArgsConstructor
@Component
@Slf4j
public class AosOverdueLetterTemplateContent implements TemplateContent {

    private final CommonContent commonContent;

    private final PaymentService paymentService;

    private final DocmosisCommonContent docmosisCommonContent;

    @Value("${aos_overdue.arrange_service_offset_days}")
    private int offsetDays;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(AOS_OVERDUE_TEMPLATE_ID, AOS_OVERDUE_JS_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return apply(caseData, caseId);
    }

    private Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        String bailiffServiceCost = formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF));
        String otherServiceCost = formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_WITHOUT_NOTICE));

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference());
        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(APPLICANT_1_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
        templateContent.put(ARRANGE_SERVICE_BY_DATE, caseData.getApplication().getIssueDate().plusDays(offsetDays)
            .format(DATE_TIME_FORMATTER));
        templateContent.put(SEARCH_ADDRESS_COST, otherServiceCost);
        templateContent.put(SERVE_BY_EMAIL_COST, otherServiceCost);
        templateContent.put(DEEMED_SERVICE_COST, otherServiceCost);
        templateContent.put(DISPENSED_SERVICE_COST, otherServiceCost);
        templateContent.put(BAILIFF_SERVICE_COST, bailiffServiceCost);

        if (caseData.isDivorce()) {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
        } else {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_CP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CP_CASE_EMAIL);
        }

        return templateContent;
    }
}
