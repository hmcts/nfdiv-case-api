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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CertificateOfEntitlementContent {

    private static final String IS_SOLE = "isSole";
    private static final String IS_JOINT = "isJoint";
    private static final String COURT_DETAILS = "courtDetails";
    private static final String APPROVAL_DATE = "approvalDate";

    @Autowired
    private ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;

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
        templateContent.put(COURT_DETAILS, conditionalOrderCourtDetails);
        templateContent.put(APPROVAL_DATE, approvalDate);

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());

        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
            caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);

        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;

        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);

        templateContent.put(HAS_FINANCIAL_ORDERS, applicant1.appliedForFinancialOrder());

        return templateContent;
    }
}
