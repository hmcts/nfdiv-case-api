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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D36;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP2;

@Component
@Slf4j
public class ApplyForFinalOrderPrinter {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    public static final String LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK = "apply-for-final-order-pack";
    private static final int EXPECTED_DOCUMENTS_SIZE = 3;

    public void sendLettersToApplicant1Offline(final CaseData caseData, final Long caseId, final Applicant applicant) {

        generateFinalOrderCoversheet(caseData, caseId, applicant);

        final List<Letter> finalOrderLettersToSend = finalOrderLetters(caseData, true);

        if (!isEmpty(finalOrderLettersToSend) && finalOrderLettersToSend.size() == EXPECTED_DOCUMENTS_SIZE) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(
                finalOrderLettersToSend,
                caseIdString,
                caseIdString,
                LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK,
                applicant.getFullName()
            );

            final UUID letterId = bulkPrintService.print(print);
            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn("Apply for Final order letters missing. Failed to send to bulk print for Case ID: {}", caseId);
        }

    }

    public void sendLettersToApplicant2Offline(final CaseData caseData, final Long caseId, final Applicant applicant) {

        generateFinalOrderCoversheet(caseData, caseId, applicant);

        final List<Letter> finalOrderLettersToSend = finalOrderLetters(caseData, false);

        if (!isEmpty(finalOrderLettersToSend) && finalOrderLettersToSend.size() == EXPECTED_DOCUMENTS_SIZE) {
            final String caseIdString = caseId.toString();
            final Print print = new Print(
                finalOrderLettersToSend,
                caseIdString,
                caseIdString,
                LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK,
                applicant.getFullName()
            );

            final UUID letterId = bulkPrintService.print(print);
            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn("Apply for Final order letters missing. Failed to send to bulk print for Case ID: {}", caseId);
        }

    }

    private List<Letter> finalOrderLetters(final CaseData caseData, boolean isApplicant1) {

        final List<Letter> coversheetLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            COVERSHEET);

        final List<Letter> canApplyFinalOrderLetters;
        if (isApplicant1) {
            canApplyFinalOrderLetters = lettersWithDocumentType(
                caseData.getDocuments().getDocumentsGenerated(),
                FINAL_ORDER_CAN_APPLY_APP1);
        } else {
            canApplyFinalOrderLetters = lettersWithDocumentType(
                caseData.getDocuments().getDocumentsGenerated(),
                FINAL_ORDER_CAN_APPLY_APP2);
        }

        final List<Letter> d36Letters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            D36
        );

        final Letter coversheetLetter = firstElement(coversheetLetters);
        final Letter canApplyFinalOrderLetter = firstElement(canApplyFinalOrderLetters);
        final Letter d36Letter = firstElement(d36Letters);

        final List<Letter> finalOrderLetters = new ArrayList<>();

        if (coversheetLetter != null) {
            finalOrderLetters.add(coversheetLetter);
        }
        if (canApplyFinalOrderLetter != null) {
            finalOrderLetters.add(canApplyFinalOrderLetter);
        }
        if (d36Letter != null) {
            finalOrderLetters.add(d36Letter);
        }

        return finalOrderLetters;
    }

    private void generateFinalOrderCoversheet(final CaseData caseData,
                                              final Long caseId,
                                              final Applicant applicant) {

        generateCoversheet.generateCoversheet(
            caseData,
            caseId,
            COVERSHEET_APPLICANT,
            coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
            applicant.getLanguagePreference()
        );
    }
}
