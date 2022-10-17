package uk.gov.hmcts.divorce.legaladvisor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent.LEGAL_ADVISOR_COMMENTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CP_CASE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GenerateCoRefusedCoverLetter {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;

    @Autowired
    private Clock clock;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public void generateAndUpdateCaseData(final CaseData caseData,
                                          final Long caseId,
                                          final String templateId,
                                          final Applicant applicant) {

        log.info("Generating Conditional Order Refused Cover Letter for case id {} ", caseId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
            templateContent(caseData, caseId, applicant),
            caseId,
            templateId,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getPostalAddress());

        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(
            LEGAL_ADVISOR_COMMENTS,
            conditionalOrderRefusedForAmendmentContent.generateLegalAdvisorComments(caseData.getConditionalOrder())
        );

        templateContent.put(
            DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL,
            caseData.isDivorce() ? CONTACT_DIVORCE_EMAIL : CP_CASE_EMAIL
        );

        return templateContent;
    }
}
