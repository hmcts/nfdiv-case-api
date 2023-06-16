package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class GenerateCertificateOfEntitlementHelper {

    public static final String GET_A_DIVORCE = "get a divorce";
    public static final String END_YOUR_CIVIL_PARTNERSHIP = "end your civil partnership";
    public static final String IS_JOINT = "isJoint";
    public static final String IS_RESPONDENT = "isRespondent";

    @Value("${final_order.eligible_from_offset_days}")
    private long finalOrderOffsetDays;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    public Map<String, Object> getTemplateContent(final CaseData caseData,
                                                  final Long caseId,
                                                  final Applicant applicant,
                                                  final Applicant partner) {

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(NAME, applicant.isRepresented()
            ? applicant.getSolicitor().getName()
            : join(" ", applicant.getFirstName(), applicant.getLastName())
        );
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(caseId));

        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, caseData.isDivorce() ? GET_A_DIVORCE :  END_YOUR_CIVIL_PARTNERSHIP);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, caseData.isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;
        final String beforeDateOfHearing = nonNull(dateAndTimeOfHearing)
            ? dateAndTimeOfHearing.minusDays(7).format(DATE_TIME_FORMATTER) : null;

        templateContent.put(COURT_NAME, conditionalOrder.getCourt() != null ? conditionalOrder.getCourt().getLabel() : null);
        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);
        if (nonNull(dateAndTimeOfHearing)) {
            templateContent.put(DATE_FO_ELIGIBLE_FROM, dateAndTimeOfHearing.plusDays(finalOrderOffsetDays).format(DATE_TIME_FORMATTER));
        } else {
            templateContent.put(DATE_FO_ELIGIBLE_FROM, null);
        }

        templateContent.put(BEFORE_DATE_OF_HEARING, beforeDateOfHearing);

        if (caseData.isJudicialSeparationCase()) {
            templateContent.put(IS_DIVORCE, caseData.isDivorce());
            templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
            templateContent.put(PARTNER, commonContent.getPartner(caseData, partner, applicant.getLanguagePreference()));
        }

        return templateContent;
    }

    public Map<String, Object> getRespondentTemplateContent(final CaseData caseData,
                                                            final Long caseId) {
        Map<String, Object> respondentTemplateContent = getTemplateContent(
            caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1());

        if (caseData.isJudicialSeparationCase()) {
            respondentTemplateContent.put(IS_RESPONDENT, true);
        } else {
            respondentTemplateContent.put(
                PARTNER,
                commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference())
            );
        }

        return respondentTemplateContent;
    }

    public Map<String, Object> getSolicitorTemplateContent(final CaseData caseData, final Long caseId, final boolean isApplicant1Solicitor,
                                                            final LanguagePreference languagePreference) {
        Map<String, Object> templateContent = docmosisCommonContent.getBasicSolicitorTemplateContent(
            caseData, caseId, isApplicant1Solicitor, languagePreference);

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;

        templateContent.put(COURT_NAME, conditionalOrder.getCourt() != null ? conditionalOrder.getCourt().getLabel() : null);
        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);

        return templateContent;
    }

}
