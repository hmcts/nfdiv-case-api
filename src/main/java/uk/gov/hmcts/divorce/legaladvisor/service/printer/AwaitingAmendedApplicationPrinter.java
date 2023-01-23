package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;

import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER;

@Component
@Slf4j
public class AwaitingAmendedApplicationPrinter {

    @Autowired
    private AwaitingAmendedOrClarificationApplicationCommonPrinter awaitingAmendedOrClarificationApplicationCommonPrinter;

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        MissingDocumentsValidation missingDocumentsValidation = new MissingDocumentsValidation(
            "Awaiting Amended Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}",
            List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL, APPLICATION),
            4
        );

        if (caseData.getIsJudicialSeparation().toBoolean()) {
            missingDocumentsValidation.message =
                "Awaiting Amended JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}";
            missingDocumentsValidation.documentTypeList = applicant.isRepresented()
                ? List.of(
                    COVERSHEET,
                    JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER,
                    CONDITIONAL_ORDER_REFUSAL,
                    APPLICATION
                )
                : List.of(
                    COVERSHEET,
                    JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
                    CONDITIONAL_ORDER_REFUSAL,
                    APPLICATION
                );
        }

        awaitingAmendedOrClarificationApplicationCommonPrinter.sendLetters(
            caseData,
            caseId,
            applicant,
            missingDocumentsValidation,
            false
        );
    }
}
