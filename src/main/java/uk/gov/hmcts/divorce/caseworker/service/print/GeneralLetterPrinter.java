package uk.gov.hmcts.divorce.caseworker.service.print;

import com.microsoft.applicationinsights.core.dependencies.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.document.DocumentUtil.mapToLetters;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;

@Component
@Slf4j
public class GeneralLetterPrinter {

    private static final String LETTER_TYPE_GENERAL_LETTER = "general-letter";

    @Autowired
    private BulkPrintService bulkPrintService;

    public void sendLetterWithAttachments(final CaseData caseData, final Long caseId) {

        ListValue<GeneralLetterDetails> generalLetterDetailsListValue = firstElement(caseData.getGeneralLetters());

        if (generalLetterDetailsListValue != null) {

            GeneralLetterDetails letterDetails = generalLetterDetailsListValue.getValue();

            List<ListValue<Document>> documents = Lists.newArrayList(ListValue.<Document>builder()
                    .value(letterDetails.getGeneralLetterLink())
                .build());

            if (!CollectionUtils.isEmpty(letterDetails.getGeneralLetterAttachmentLinks())) {
                documents.addAll(letterDetails.getGeneralLetterAttachmentLinks());
            }

            final String caseIdString = caseId.toString();

            final Print print = new Print(mapToLetters(documents, GENERAL_LETTER), caseIdString, caseIdString, LETTER_TYPE_GENERAL_LETTER);

            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn("No general letters found for print , for Case ID: {}", caseId);
        }
    }
}
