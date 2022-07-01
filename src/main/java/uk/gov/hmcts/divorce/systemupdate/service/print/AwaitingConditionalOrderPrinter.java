package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import java.util.ArrayList;
import java.util.UUID;

import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;

@Component
@Slf4j
public class AwaitingConditionalOrderPrinter {

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private GenerateD84Form generateD84Form;

    @Autowired
    GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    private static final String LETTER_TYPE_AWAITING_CONDITIONAL_ORDER_PACK = "awaiting-conditional-order-pack";

    public void sendLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {

        generateLetters(caseData, caseId, applicant);

        final String caseIdString = caseId.toString();
        final Print print = new Print(
            new ArrayList<>(),
            caseIdString,
            caseIdString,
            LETTER_TYPE_AWAITING_CONDITIONAL_ORDER_PACK);
        final UUID letterId = bulkPrintService.print(print);
        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private void generateLetters(final CaseData caseData, final Long caseId, final Applicant applicant) {
        generateCoversheet.generateCoversheet(
            caseData,
            caseId,
            COVERSHEET_APPLICANT,
            coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
            caseData.getApplicant2().getLanguagePreference()
        );
        generateD84Form.generateD84Document(caseData, caseId);
        // TODO - generateConditionalOrderDocument
    }
}
