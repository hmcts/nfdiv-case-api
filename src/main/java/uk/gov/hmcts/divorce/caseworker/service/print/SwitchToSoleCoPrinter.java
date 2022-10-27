package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;

@Component
@Slf4j
public class SwitchToSoleCoPrinter {

    private static final String LETTER_TYPE_SWITCH_TO_SOLE_CO = "switch-to-sole-co-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void print(final CaseData caseData, final Long caseId) {

        final List<Letter> switchToSoleCoLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            SWITCH_TO_SOLE_CO_LETTER);

        final Letter switchToSoleCoLetter = firstElement(switchToSoleCoLetters);

        if (!isEmpty(switchToSoleCoLetter)) {

            final String caseIdString = caseId.toString();
            final Print print =
                new Print(singletonList(switchToSoleCoLetter), caseIdString, caseIdString, LETTER_TYPE_SWITCH_TO_SOLE_CO);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Switch To Sole Conditional Order has missing documents. Expected document with type {} , for Case ID: {}",
                List.of(SWITCH_TO_SOLE_CO_LETTER),
                caseId
            );
        }
    }


}
