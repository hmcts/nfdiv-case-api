package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ConditionalOrderCourtDetails;
import uk.gov.hmcts.divorce.common.config.ConditionalOrderCourtDetailsConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateOfEntitlementTemplateContent implements TemplateContent {

    private static final String IS_SOLE = "isSole";
    private static final String IS_JOINT = "isJoint";
    private static final String COURT_DETAILS = "courtDetails";
    private static final String APPROVAL_DATE = "approvalDate";

    private final ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {

        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
                applicant1.getLanguagePreference());

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

        if (caseData.isJudicialSeparationCase()) {
            templateContent.put(IS_DIVORCE, caseData.isDivorce());
            templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        }

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());

        if (WELSH.equals(applicant1.getLanguagePreference())) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
                    caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE_CY : CIVIL_PARTNERSHIP_CY);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
                    caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);
        }

        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;
        final String beforeDateOfHearing = nonNull(dateAndTimeOfHearing)
                ? dateAndTimeOfHearing.minusDays(7).format(DATE_TIME_FORMATTER) : null;

        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);
        templateContent.put(BEFORE_DATE_OF_HEARING, beforeDateOfHearing);

        templateContent.put(HAS_FINANCIAL_ORDERS, applicant1.appliedForFinancialOrder());

        if (applicant != null && applicant.isRepresented()) {
            if (caseData.getApplicant1().isRepresented()) {
                templateContent.put(APPLICANT_1_SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
            }
            if (caseData.getApplicant2().isRepresented()) {
                templateContent.put(APPLICANT_2_SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
            }
            templateContent.put(COURT_NAME, conditionalOrderCourt);

            templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
            templateContent.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
            templateContent.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
            templateContent.put(SOLICITOR_REFERENCE, Objects.nonNull(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : "not provided");
        }

        return templateContent;
    }
}
