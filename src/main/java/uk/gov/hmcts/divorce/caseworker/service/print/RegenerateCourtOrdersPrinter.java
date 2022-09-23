package uk.gov.hmcts.divorce.caseworker.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;

@Component
@Slf4j
public class RegenerateCourtOrdersPrinter {

    private static final String LETTER_TYPE_REGENERATE_COURT_ORDERS = "regenerate-court-orders-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void print(final CaseData caseData, final Long caseId, final DocumentType coversheetDocumentType, final boolean isApplicant1) {

        final List<Letter> regeneratedCourtOrderLetters = regeneratedCourtOrderLetters(caseData);

        if (!isEmpty(regeneratedCourtOrderLetters)) {

            final String caseIdString = caseId.toString();
            final Print print =
                new Print(regeneratedCourtOrderLetters, caseIdString, caseIdString, LETTER_TYPE_REGENERATE_COURT_ORDERS);
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

//    private List<Letter> regeneratedCourtOrderLetters(CaseData caseData) {
//
//        // isApplicant1 ? CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1 : CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2
//        // CONDITIONAL_ORDER_GRANTED
//        // CERTIFICATE_OF_ENTITLEMENT
//        // FINAL_ORDER_GRANTED
//
//
//        final List<Letter> regeneratedCourtOrderLetters = new ArrayList<>();
//
//        if (null != coversheetLetter) {
//            regeneratedCourtOrderLetters.add(coversheetLetter);
//        }
//        if (null != conditionalOrderGrantedLetter) {
//            regeneratedCourtOrderLetters.add(conditionalOrderGrantedLetter);
//        }
//        return regeneratedCourtOrderLetters;
//    }
}
