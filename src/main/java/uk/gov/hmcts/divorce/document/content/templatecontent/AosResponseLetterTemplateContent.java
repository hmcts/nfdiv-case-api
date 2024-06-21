package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@RequiredArgsConstructor
public class AosResponseLetterTemplateContent implements TemplateContent {

    public static final String RELATION = "relation";
    public static final String CIVIL_PARTNER = "civil partner";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION = "divorceOrEndCivilPartnershipApplication";
    public static final String DIVORCE_APPLICATION = "divorce application";
    public static final String APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP = "application to end your civil partnership";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL = "divorceOrCivilPartnershipEmail";
    public static final String DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS = "divorceOrCivilPartnershipProceedings";
    public static final String DIVORCE_PROCEEDINGS = "divorce proceedings";
    public static final String PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP = "proceedings to end your civil partnership";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS = "divorceOrEndCivilPartnershipProcess";
    public static final String WAIT_UNTIL_DATE = "waitUntilDate";

    private final CommonContent commonContent;
    private final HoldingPeriodService holdingPeriodService;
    private final DocmosisCommonContent docmosisCommonContent;
    private final Clock clock;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED,
            NFD_NOP_APP1_JS_SOLE_DISPUTED,
            NFD_NOP_APP1_JS_SOLE_UNDISPUTED,
            RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return apply(caseData, caseId);
    }

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference());

        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());
        templateContent.put(APPLICANT_1_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(ISSUE_DATE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DUE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        templateContent.put(
            WAIT_UNTIL_DATE,
            holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()).format(DATE_TIME_FORMATTER)
        );

        if (caseData.isDivorce()) {
            templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, DIVORCE_SERVICE);
        } else {
            templateContent.put(RELATION, CIVIL_PARTNER);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_CP_SERVICE);
        }

        if (caseData.isJudicialSeparationCase()) {
            templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
            templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());
            templateContent.put(IS_DIVORCE, caseData.isDivorce());
            templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
            templateContent.put(RECIPIENT_NAME, caseData.getApplicant1().isRepresented()
                ? caseData.getApplicant1().getSolicitor().getName() : caseData.getApplicant1().getFullName());
            templateContent.put(RECIPIENT_ADDRESS, caseData.getApplicant1().isRepresented()
                ? caseData.getApplicant1().getSolicitor().getAddress()
                : caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());
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
