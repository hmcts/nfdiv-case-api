package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GenerateFinalOrderCoverLetter {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public void apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplicant1().isApplicantOffline()) {
            log.info("Generating final order cover letter for Applicant 1 for case id: {} ", caseId);
            generateFinalOrderCoverLetter(
                caseData,
                caseId,
                caseData.getApplicant1(),
                FINAL_ORDER_GRANTED_COVER_LETTER_APP_1
            );
        }

        if (isBlank(caseData.getApplicant2EmailAddress()) || caseData.getApplicant2().isApplicantOffline()) {
            log.info("Generating final order cover letter for Applicant 2 for case id: {} ", caseId);
            generateFinalOrderCoverLetter(
                caseData,
                caseId,
                caseData.getApplicant2(),
                FINAL_ORDER_GRANTED_COVER_LETTER_APP_2
            );
        }
    }

    public void generateFinalOrderCoverLetter(final CaseData caseData,
                                              final Long caseId,
                                              final Applicant applicant,
                                              final DocumentType coverLetterDocumentType) {

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            coverLetterDocumentType,
            templateContent(caseData, caseId, applicant),
            caseId,
            FINAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());

        return templateContent;
    }

    public void removeExistingAndGenerateNewFinalOrderGrantedCoverLetters(CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();
        final List<DocumentType> documentTypesToRemove =
            List.of(FINAL_ORDER_GRANTED_COVER_LETTER_APP_1, FINAL_ORDER_GRANTED_COVER_LETTER_APP_2);

        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                .removeIf(document -> documentTypesToRemove.contains(document.getValue().getDocumentType()));
        }

        apply(caseDetails);
    }
}
