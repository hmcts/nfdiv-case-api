package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class JudicialSeparationSwitchToSoleSolicitorContent {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private Clock clock;



    public void apply(final CaseData caseData,
                      final Long caseId,
                      final Applicant applicant,
                      final Applicant respondent) {

        log.info("Generating JS Switched to sole cover letter for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            SWITCH_TO_SOLE_CO_LETTER,
            templateContent(caseData, caseId, applicant, respondent),
            caseId,
            JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant,
                                                final Applicant respondent) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(RESPONDENT_SOLICITOR_NAME, respondent.getSolicitor().getName());
        templateContent.put(RESPONDENT_SOLICITOR_ADDRESS, respondent.getSolicitor().getAddress());
        templateContent.put(CASE_REFERENCE, formatId(caseId));

        templateContent.put(APPLICANT_1_FULL_NAME, applicant.getFullName());
        templateContent.put(RESPONDENT_FULL_NAME, respondent.getFullName());
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
            caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);
        templateContent.put(SOLICITOR_REFERENCE, isNotEmpty(respondent.getSolicitor().getReference())
            ? respondent.getSolicitor().getReference()
            : NOT_PROVIDED);

        templateContent.put(APPLICANT_1_SOLICITOR_NAME, applicant.isRepresented()
            ? applicant.getSolicitor().getName()
            : NOT_REPRESENTED);

        templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant1(),
            caseData.getApplicant2().getLanguagePreference()));

        return templateContent;
    }
}
