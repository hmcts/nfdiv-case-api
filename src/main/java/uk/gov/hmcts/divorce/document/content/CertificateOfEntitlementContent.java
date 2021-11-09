package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.ConditionalOrderCourtDetailsConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Locale.UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COSTS_GRANTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CertificateOfEntitlementContent {

    @Autowired
    private ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;

    public Map<String, Object> apply(final CaseData caseData, final Long caseId) {

        final Map<String, Object> templateContent = new HashMap<>();

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put("courtDetails", conditionalOrderCourtDetailsConfig.get(conditionalOrder.getCourt().getCourtId()));
        templateContent.put("approvalDate", conditionalOrder.getDecisionDate().format(DATE_TIME_FORMATTER));

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());

        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());

        templateContent.put("isSole", caseData.getApplicationType().isSole());
        templateContent.put("isJoint", !caseData.getApplicationType().isSole());

        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        templateContent.put(DATE_OF_HEARING, dateAndTimeOfHearing.format(DATE_TIME_FORMATTER));
        templateContent.put(TIME_OF_HEARING, dateAndTimeOfHearing.format(TIME_FORMATTER));

        templateContent.put(HAS_FINANCIAL_ORDERS, applicant1.getFinancialOrder().toBoolean());
        templateContent.put(COSTS_GRANTED, conditionalOrder.getClaimsGranted().toBoolean());
        templateContent.put("claimsCostsOrderInformation", conditionalOrder.getClaimsCostsOrderInformation());
        templateContent.put("divorceWho", caseData.getApplication().getDivorceWho().getLabel().toLowerCase(UK));

        return templateContent;
    }
}
