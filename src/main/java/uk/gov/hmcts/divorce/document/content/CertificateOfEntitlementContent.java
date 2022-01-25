package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ConditionalOrderCourtDetails;
import uk.gov.hmcts.divorce.common.config.ConditionalOrderCourtDetailsConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
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

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData, final Long caseId) {

        final Map<String, Object> templateContent = new HashMap<>();

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        final ConditionalOrderCourt conditionalOrderCourt = conditionalOrder.getCourt();
        final ConditionalOrderCourtDetails conditionalOrderCourtDetails = nonNull(conditionalOrderCourt)
            ? conditionalOrderCourtDetailsConfig.get(conditionalOrderCourt.getCourtId())
            : null;

        final LocalDate decisionDate = conditionalOrder.getDecisionDate();
        final String approvalDate = nonNull(decisionDate)
            ? decisionDate.format(DATE_TIME_FORMATTER)
            : null;

        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put("courtDetails", conditionalOrderCourtDetails);
        templateContent.put("approvalDate", approvalDate);

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());

        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());

        templateContent.put("isSole", caseData.getApplicationType().isSole());
        templateContent.put("isJoint", !caseData.getApplicationType().isSole());

        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;

        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);

        templateContent.put(HAS_FINANCIAL_ORDERS, applicant1.appliedForFinancialOrder());

        final boolean claimsGranted = conditionalOrder.areClaimsGranted();
        templateContent.put(COSTS_GRANTED, claimsGranted);
        if (claimsGranted) {
            templateContent.put("claimsCostsOrderInformation", conditionalOrder.getClaimsCostsOrderInformation());
        }

        templateContent.put("divorceWho", commonContent.getPartner(caseData, caseData.getApplicant2()));

        return templateContent;
    }
}
