package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class ConditionalOrderPronouncedPrinter {

    private static final String LETTER_TYPE_CO_PRONOUNCED = "conditional-order-pronounced";
    private static final String NAME = "name";
    private static final String ADDRESS = "address";
    private static final String PRONOUNCEMENT_DATE_PLUS_43 = "pronouncementDatePlus43";

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    public void sendLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {

        generateCoversheet(caseData, caseId, applicant);

        final List<Letter> conditionalOrderPronouncedLetters = conditionalOrderPronouncedLetters(caseData);

        if (!isEmpty(conditionalOrderPronouncedLetters)) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(conditionalOrderPronouncedLetters, caseIdString, caseIdString, LETTER_TYPE_CO_PRONOUNCED);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Conditional Order Pronounced print has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(CONDITIONAL_ORDER_GRANTED_COVERSHEET, CONDITIONAL_ORDER_GRANTED),
                caseId);
        }
    }

    private List<Letter> conditionalOrderPronouncedLetters(CaseData caseData) {
        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET);

        final List<Letter> conditionalOrderGrantedLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_GRANTED);

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter conditionalOrderGrantedLetter = firstElement(conditionalOrderGrantedLetters);

        final List<Letter> currentConditionalOrderPronouncedLetters = new ArrayList<>();

        if (null != coversheetLetter) {
            currentConditionalOrderPronouncedLetters.add(coversheetLetter);
        }
        if (null != conditionalOrderGrantedLetter) {
            currentConditionalOrderPronouncedLetters.add(conditionalOrderGrantedLetter);
        }
        return currentConditionalOrderPronouncedLetters;
    }

    private void generateCoversheet(final CaseData caseData,
                                    final Long caseId,
                                    final Applicant applicant) {

        log.info("Generating order coversheet for sole case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED_COVERSHEET,
            coversheetContent(caseData, caseId, applicant),
            caseId,
            CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }

    private Map<String, Object> coversheetContent(final CaseData caseData,
                                                  final Long caseId,
                                                  final Applicant applicant) {

        final Map<String, Object> templateContent = new HashMap<>();

        if (applicant.isRepresented()) {
            Solicitor applicantSolicitor = applicant.getSolicitor();
            templateContent.put(NAME, applicantSolicitor.getName());
            templateContent.put(ADDRESS, applicantSolicitor.getAddress());
        } else {
            templateContent.put(NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
            templateContent.put(ADDRESS, applicant.getAddress());
        }
        templateContent.put(CASE_REFERENCE, caseId != null ? formatId(caseId) : null);
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
            caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);
        templateContent.put(PRONOUNCEMENT_DATE_PLUS_43,
            caseData.getConditionalOrder().getGrantedDate() != null
                ? caseData.getConditionalOrder().getGrantedDate().plusDays(43).format(DATE_TIME_FORMATTER)
                : null
        );

        return templateContent;
    }
}
