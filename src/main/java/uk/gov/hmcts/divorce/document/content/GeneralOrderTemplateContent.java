package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.ASSISTANT_JUSTICES_CLERK;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.DEPUTY_DISTRICT_JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.DISTRICT_JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.PROPER_OFFICER_OF_THE_COURT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType.RECORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_MADE_BY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SITTING_VENUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class GeneralOrderTemplateContent {

    private static final String APPLICANT = "Applicant";
    private static final String APPLICANT_1 = "Applicant 1";
    private static final String APPLICANT_2 = "Applicant 2";
    private static final String RESPONDENT = "Respondent";
    private static final String SITTING = "sitting";
    private static final String SITTING_CONTENT = ", sitting";
    private static final String JUDGE_NAME = "%s %s";
    private static final String AN_ASSISTANT_JUDGES_CLERK = "an %s";
    private static final String A_PROPER_OFFICER_OF_THE_COURT = "a %s";

    private static final String RECORDER_NAME = "%s %s";

    @Autowired
    private Clock clock;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference());

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        var generalOrder = caseData.getGeneralOrder();
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(GENERAL_ORDER_DATE, generalOrder.getGeneralOrderDate().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, ccdCaseReference);
        templateContent.put(PETITIONER_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(RESPONDENT_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(GENERAL_ORDER_DETAILS, generalOrder.getGeneralOrderDetails());

        if (StringUtils.isNotBlank(generalOrder.getGeneralOrderRecitals())) {
            templateContent.put(GENERAL_ORDER_RECITALS, generalOrder.getGeneralOrderRecitals());
        }

        if (DEPUTY_DISTRICT_JUDGE.equals(generalOrder.getGeneralOrderJudgeOrLegalAdvisorType())
            || DISTRICT_JUDGE.equals(generalOrder.getGeneralOrderJudgeOrLegalAdvisorType())
            || HER_HONOUR_JUDGE.equals(generalOrder.getGeneralOrderJudgeOrLegalAdvisorType())
            || HIS_HONOUR_JUDGE.equals(generalOrder.getGeneralOrderJudgeOrLegalAdvisorType())) {
            templateContent.put(GENERAL_ORDER_MADE_BY,
                String.format(JUDGE_NAME, generalOrder.getGeneralOrderJudgeOrLegalAdvisorType().getLabel(),
                    generalOrder.getGeneralOrderJudgeOrLegalAdvisorName()));
            templateContent.put(SITTING, SITTING_CONTENT);
        } else if (ASSISTANT_JUSTICES_CLERK.equals(generalOrder.getGeneralOrderJudgeOrLegalAdvisorType())) {
            templateContent.put(GENERAL_ORDER_MADE_BY,
                String.format(AN_ASSISTANT_JUDGES_CLERK, ASSISTANT_JUSTICES_CLERK.getLabel())
            );
        } else if (PROPER_OFFICER_OF_THE_COURT.equals(generalOrder.getGeneralOrderJudgeOrLegalAdvisorType())) {
            templateContent.put(GENERAL_ORDER_MADE_BY,
                String.format(A_PROPER_OFFICER_OF_THE_COURT, PROPER_OFFICER_OF_THE_COURT.getLabel()));
        } else if (RECORDER.equals(generalOrder.getGeneralOrderJudgeOrLegalAdvisorType())) {
            templateContent.put(GENERAL_ORDER_MADE_BY,
                String.format(RECORDER_NAME, generalOrder.getGeneralOrderJudgeOrLegalAdvisorType().getLabel(),
                    generalOrder.getGeneralOrderJudgeOrLegalAdvisorName()));
        }

        if (caseData.getApplicationType().isSole()) {
            templateContent.put(APPLICANT_HEADING, APPLICANT);
            templateContent.put(RESPONDENT_HEADING, RESPONDENT);
        } else {
            templateContent.put(APPLICANT_HEADING, APPLICANT_1);
            templateContent.put(RESPONDENT_HEADING, APPLICANT_2);
        }

        if (StringUtils.isNotBlank(generalOrder.getGeneralOrderJudgeOrLegalAdvisorVenue())) {
            templateContent.put(SITTING_VENUE, generalOrder.getGeneralOrderJudgeOrLegalAdvisorVenue());
        }

        return templateContent;
    }
}
