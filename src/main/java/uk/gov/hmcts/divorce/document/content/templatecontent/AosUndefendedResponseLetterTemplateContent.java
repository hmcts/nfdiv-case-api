package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CP_CASE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@RequiredArgsConstructor
public class AosUndefendedResponseLetterTemplateContent implements TemplateContent {

    private static final String DATE_TO_WAIT_UNTIL_APPLY_FOR_CO = "dateToWaitUntilApplyForCO";

    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;
    private final HoldingPeriodService holdingPeriodService;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED,
            RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return apply(caseData, caseId);
    }

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1()
            .getLanguagePreference());
        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(APPLICANT_1_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
        templateContent.put(DATE_TO_WAIT_UNTIL_APPLY_FOR_CO,
            holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate())
                .format(DATE_TIME_FORMATTER));

        if (caseData.isDivorce()) {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, DIVORCE_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
        } else {
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_CP_SERVICE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CP_CASE_EMAIL);
        }

        if (caseData.isJudicialSeparationCase()) {
            templateContent.put(RECIPIENT_NAME, caseData.getApplicant1().isRepresented()
                ? caseData.getApplicant1().getSolicitor().getName() : caseData.getApplicant1().getFullName());
            templateContent.put(RECIPIENT_ADDRESS, caseData.getApplicant1().isRepresented()
                ? caseData.getApplicant1().getSolicitor().getAddress()
                : caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());
            templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
            templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());
            templateContent.put(SOLICITOR_NAME, caseData.getApplicant1().isRepresented()
                ? caseData.getApplicant1().getSolicitor().getName() : NOT_REPRESENTED);
            templateContent.put(APPLICANT_2_SOLICITOR_NAME, caseData.getApplicant2().isRepresented()
                ? caseData.getApplicant2().getSolicitor().getName() : NOT_REPRESENTED);
            templateContent.put(SOLICITOR_REFERENCE, caseData.getApplicant1().isRepresented()
                ? caseData.getApplicant1().getSolicitor().getReference() : NOT_REPRESENTED);
        }

        return templateContent;
    }
}
