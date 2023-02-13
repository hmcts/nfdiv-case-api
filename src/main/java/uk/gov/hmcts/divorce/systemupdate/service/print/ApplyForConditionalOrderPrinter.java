package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateApplyForConditionalOrderDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_CAN_APPLY;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;

@Component
@Slf4j
public class ApplyForConditionalOrderPrinter {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateD84Form generateD84Form;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Autowired
    private GenerateApplyForConditionalOrderDocument generateApplyForConditionalOrderDocument;

    private static final String LETTER_TYPE_APPLY_FOR_CONDITIONAL_ORDER_PACK = "apply-for-conditional-order-pack";

    private static final int EXPECTED_DOCUMENTS_SIZE = 3;

    public void sendLetters(final CaseData caseData,
                            final Long caseId,
                            final Applicant applicant,
                            final Applicant partner) {

        generateLetters(caseData, caseId, applicant, partner);
        final List<Letter> conditionalOrderLettersToSend = conditionalOrderLetters(caseData);

        if (!isEmpty(conditionalOrderLettersToSend) && conditionalOrderLettersToSend.size() == EXPECTED_DOCUMENTS_SIZE) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(
                conditionalOrderLettersToSend,
                caseIdString,
                caseIdString,
                LETTER_TYPE_APPLY_FOR_CONDITIONAL_ORDER_PACK);

            final UUID letterId = bulkPrintService.print(print);
            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn("Apply for Conditional order letters missing. Failed to send to bulk print for Case ID: {}", caseId);
        }
    }

    private List<Letter> conditionalOrderLetters(final CaseData caseData) {

        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET);

        final List<Letter> canApplyConditionalOrderLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_CAN_APPLY);

        final List<Letter> d84Letters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            D84
        );

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter canApplyConditionalOrderLetter = firstElement(canApplyConditionalOrderLetters);
        final Letter d84Letter = firstElement(d84Letters);

        final List<Letter> conditionalOrderLetters = new ArrayList<>();

        if (coversheetLetter != null) {
            conditionalOrderLetters.add(coversheetLetter);
        }
        if (canApplyConditionalOrderLetter != null) {
            conditionalOrderLetters.add(canApplyConditionalOrderLetter);
        }
        if (d84Letter != null) {
            conditionalOrderLetters.add(d84Letter);
        }

        return conditionalOrderLetters;
    }

    private void generateLetters(final CaseData caseData,
                                 final Long caseId,
                                 final Applicant applicant,
                                 final Applicant partner) {

        generateCoversheet.generateCoversheet(
            caseData,
            caseId,
            COVERSHEET_APPLICANT,
            coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
            applicant.getLanguagePreference()
        );
        generateD84Form.generateD84Document(caseData, caseId);
        generateApplyForConditionalOrderDocument.generateApplyForConditionalOrder(caseData, caseId, applicant, partner);
    }
}
