package uk.gov.hmcts.divorce.citizen.notification.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.CO_SUBMISSION_DATE_PLUS_DAYS;
import static uk.gov.hmcts.divorce.notification.CommonContent.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PRONOUNCE_BY_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class AppliedForConditionalOrderNotification {

    static final String PLUS_14_DUE_DATE = "application for CO date plus 14 days";
    static final String WIFE_APPLIED = "wifeApplied";
    static final String HUSBAND_APPLIED = "husbandApplied";
    static final String CIVIL_PARTNER_APPLIED = "civilPartnerApplied";
    static final String PARTNER_APPLIED = "partnerApplied";
    static final String WIFE_DID_NOT_APPLY = "wifeDidNotApply";
    static final String HUSBAND_DID_NOT_APPLY = "husbandDidNotApply";
    static final String CIVIL_PARTNER_DID_NOT_APPLY = "civilPartnerDidNotApply";
    static final String PARTNER_DID_NOT_APPLY = "partnerDidNotApply";
    static final String PARTNER_DID_NOT_APPLY_DUE_DATE = "partnerDidNotApply due date";
    static final String RESPONSE_DUE_DATE = "responseDueDate";
    static final String CO_OR_FO = "coOrFo";
    static final String APPLICANT1 = "applicant 1";
    static final String APPLICANT2 = "applicant 2";

    private final CommonContent commonContent;

    public AppliedForConditionalOrderNotification(CommonContent commonContent) {
        this.commonContent = commonContent;
    }

    protected Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner, String whichApplicant) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(PRONOUNCE_BY_DATE,
            coQuestions(caseData, whichApplicant).getSubmittedDate().plusDays(CO_SUBMISSION_DATE_PLUS_DAYS).format(DATE_TIME_FORMATTER));
        if (!caseData.getApplicationType().isSole()) {
            templateVars.putAll(jointTemplateVars(caseData, partner, whichApplicant));
        }
        return templateVars;
    }

    protected Map<String, String> solicitorTemplateVars(CaseData caseData, Long id, Solicitor solicitor) {
        Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, id);
        templateVars.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(SOLICITOR_NAME, solicitor.getName());
        templateVars.put(
            SOLICITOR_REFERENCE,
            Objects.nonNull(solicitor.getReference()) ? solicitor.getReference() : "not provided"
        );
        return templateVars;
    }

    protected Map<String, String> solicitorTemplateVars(CaseData data, Long id, Applicant applicant, String whichPartner) {
        Map<String, String> templateVars = commonContent.basicTemplateVars(data, id);
        templateVars.put(DocmosisTemplateConstants.ISSUE_DATE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateVars.put(RESPONSE_DUE_DATE,
            coQuestions(data, whichPartner).getSubmittedDate().plusDays(14).format(DATE_TIME_FORMATTER));
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : NOT_PROVIDED);
        templateVars.put(CO_OR_FO, "conditional");
        templateVars.put(APPLICANT_1_FULL_NAME, data.getApplicant1().getFullName());
        templateVars.put(APPLICANT_2_FULL_NAME, data.getApplicant2().getFullName());
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(id));
        return templateVars;
    }

    protected Map<String, String> partnerTemplateVars(CaseData data, Long id, Applicant applicant, Applicant partner, String whichPartner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(data, id, applicant, partner);
        templateVars.put(PLUS_14_DUE_DATE,
            coQuestions(data, whichPartner).getSubmittedDate().plusDays(14).format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    protected boolean alreadyApplied(CaseData caseData, String whichApplicant) {
        return Objects.nonNull(coQuestions(caseData, whichApplicant).getSubmittedDate());
    }

    private Map<String, String> jointTemplateVars(CaseData data, Applicant partner, String whichApplicant) {
        Map<String, String> templateVars = defaultJointTemplateVars();
        if (alreadyApplied(data, whichPartner(whichApplicant))) {
            templateVars.putAll(divorceDissolutionJointTemplateVars(
                partner,
                data.isDivorce(),
                WIFE_APPLIED,
                HUSBAND_APPLIED,
                CIVIL_PARTNER_APPLIED
            ));
            templateVars.put(PARTNER_APPLIED, YES);
        } else {
            templateVars.putAll(divorceDissolutionJointTemplateVars(
                partner,
                data.isDivorce(),
                WIFE_DID_NOT_APPLY,
                HUSBAND_DID_NOT_APPLY,
                CIVIL_PARTNER_DID_NOT_APPLY
            ));
            templateVars.put(PARTNER_DID_NOT_APPLY, YES);
            templateVars.put(PARTNER_DID_NOT_APPLY_DUE_DATE,
                coQuestions(data, whichApplicant).getSubmittedDate().plusDays(14).format(DATE_TIME_FORMATTER));
        }
        return templateVars;
    }

    private Map<String, String> divorceDissolutionJointTemplateVars(
        Applicant partner, boolean isDivorce, String wifeVar, String husbandVar, String civilPartnerVar
    ) {
        Map<String, String> templateVars = new HashMap<>();
        if (isDivorce) {
            templateVars.put(wifeVar, Gender.FEMALE.equals(partner.getGender()) ? YES : NO);
            templateVars.put(husbandVar, Gender.MALE.equals(partner.getGender()) ? YES : NO);
        } else {
            templateVars.put(civilPartnerVar, YES);
        }
        return templateVars;
    }

    private Map<String, String> defaultJointTemplateVars() {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(WIFE_APPLIED, NO);
        templateVars.put(HUSBAND_APPLIED, NO);
        templateVars.put(CIVIL_PARTNER_APPLIED, NO);
        templateVars.put(PARTNER_APPLIED, NO);
        templateVars.put(WIFE_DID_NOT_APPLY, NO);
        templateVars.put(HUSBAND_DID_NOT_APPLY, NO);
        templateVars.put(CIVIL_PARTNER_DID_NOT_APPLY, NO);
        templateVars.put(PARTNER_DID_NOT_APPLY, NO);
        templateVars.put(PARTNER_DID_NOT_APPLY_DUE_DATE, "");
        return templateVars;
    }

    private ConditionalOrderQuestions coQuestions(CaseData caseData, String whichApplicant) {
        return whichApplicant.equalsIgnoreCase(APPLICANT1)
            ? caseData.getConditionalOrder().getConditionalOrderApplicant1Questions()
            : caseData.getConditionalOrder().getConditionalOrderApplicant2Questions();
    }

    private String whichPartner(String whichApplicant) {
        return whichApplicant.equalsIgnoreCase(APPLICANT1) ? APPLICANT2 : APPLICANT1;
    }
}
