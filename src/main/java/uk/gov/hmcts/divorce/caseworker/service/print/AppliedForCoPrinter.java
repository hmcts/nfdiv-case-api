package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLIED_FOR_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class AppliedForCoPrinter {

    public static final String NAME = "name";
    public static final String DATE_D84_RECEIVED = "dateD84Received";
    public static final String GRANTED_DATE = "grantedDate";
    private static final String LETTER_TYPE_APPLIED_FOR_CO = "applied-for-co-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    public void print(final CaseData caseData,
                      final Long caseId,
                      final Applicant applicant) {

        generateAppliedForCoLetter(caseData, caseId, applicant);

        final List<Letter> appliedForCoLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            APPLIED_FOR_CO_LETTER);

        final Letter appliedForCoLetter = firstElement(appliedForCoLetters);

        if (!isEmpty(appliedForCoLetter)) {

            final String caseIdString = caseId.toString();
            final Print print =
                new Print(
                    singletonList(appliedForCoLetter),
                    caseIdString,
                    caseIdString,
                    LETTER_TYPE_APPLIED_FOR_CO,
                    applicant.getFullName()
                );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Applied for Conditional Order has missing documents. Expected document with type {} , for Case ID: {}",
                List.of(APPLIED_FOR_CO_LETTER),
                caseId
            );
        }
    }

    private void generateAppliedForCoLetter(final CaseData caseData,
                                            final Long caseId,
                                            final Applicant applicant) {

        log.info("Generating coversheet for sole case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            APPLIED_FOR_CO_LETTER,
            templateContent(caseData, caseId, applicant),
            caseId,
            APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, APPLIED_FOR_CONDITIONAL_ORDER_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {

        final Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(DATE_D84_RECEIVED, caseData.getConditionalOrder().getDateD84FormScanned().format(DATE_TIME_FORMATTER));
        templateContent.put(GRANTED_DATE, now(clock).plusWeeks(4).format(DATE_TIME_FORMATTER));

        return templateContent;
    }
}
