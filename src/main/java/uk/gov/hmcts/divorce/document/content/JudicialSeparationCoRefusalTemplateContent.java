package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class JudicialSeparationCoRefusalTemplateContent {

    private static final String IS_JOINT = "isJoint";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    public void generateAndUpdateCaseData(final CaseData caseData,
                                          final Long caseId,
                                          final Applicant applicant) {

        if (caseData.isDivorce()) {
            log.info("Generating Judicial Separation Order Refused Cover Letter for case id {} ", caseId);
        } else {
            log.info("Generating Separation Order Refused Cover Letter for case id {} ", caseId);
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            getCoverLetterDocumentType(caseData, applicant),
            templateContent(caseData, caseId, applicant),
            caseId,
            getCoverLetterDocumentTemplateId(caseData, applicant),
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    public DocumentType getCoverLetterDocumentType(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, false);
    }

    public String getCoverLetterDocumentTemplateId(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, false);
    }

    private String getSolicitorName(final Applicant applicant) {
        if (applicant.isRepresented()) {
            return applicant.getSolicitor().getName();
        }
        return WELSH.equals(applicant.getLanguagePreference())
            ? "nas cynrychiolwyd"
            : "not represented";
    }

    private String getSolicitorReference(final Applicant applicant) {
        if (applicant.isRepresented() && applicant.getSolicitor().getReference() != null) {
            return applicant.getSolicitor().getReference();
        }
        return WELSH.equals(applicant.getLanguagePreference())
            ? "heb ei ddarparu"
            : "not provided";
    }

    public Map<String, Object> templateContent(final CaseData caseData, final Long ccdCaseReference, final Applicant applicant) {

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(FEEDBACK, conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()));

        if (applicant.isRepresented()) {
            templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
            templateContent.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
            templateContent.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
            templateContent.put(SOLICITOR_REFERENCE, getSolicitorReference(applicant));
            templateContent.put(APPLICANT_1_SOLICITOR_NAME, getSolicitorName(caseData.getApplicant1()));
            templateContent.put(APPLICANT_2_SOLICITOR_NAME, getSolicitorName(caseData.getApplicant2()));
            templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
            templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        } else {
            templateContent.put(FIRST_NAME, applicant.getFirstName());
            templateContent.put(LAST_NAME, applicant.getLastName());
            templateContent.put(ADDRESS, applicant.getPostalAddress());

            if (caseData.getDivorceOrDissolution().isDivorce()) {
                templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
                templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
            } else {
                templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
                templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            }

            templateContent.put(PARTNER, conditionalOrderCommonContent.getPartner(caseData));
        }

        return templateContent;
    }
}
