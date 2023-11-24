package uk.gov.hmcts.divorce.document.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentUtil.mapToLetters;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class LetterPrinter {

    private final DocumentGenerator documentGenerator;
    private final BulkPrintService bulkPrintService;

    public void sendLetters(final CaseData caseData,
                            final Long caseId,
                            final Applicant applicant,
                            final DocumentPackInfo documentPackInfo,
                            final String letterName) {

        List<Letter> letters = documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo);

        if (!isEmpty(letters) && letters.size() == documentPackInfo.documentPack().size()) {

            if ("general-letter".equals(letterName)) {
                letters.addAll(addAnyAttachmentsToPackForGeneralLetter(caseData));
            }

            final String caseIdString = caseId.toString();
            final Print print = new Print(
                letters,
                caseIdString,
                caseIdString,
                letterName,
                applicant.getFullName()
            );
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            throw new IllegalArgumentException(
                "%s letter pack has missing documents. Expected documents with type %s for case %s".formatted(
                    letterName,
                    documentPackInfo.documentPack().keySet(),
                    caseId)
            );
        }
    }

    private List<Letter> addAnyAttachmentsToPackForGeneralLetter(final CaseData caseData) {

        ListValue<GeneralLetterDetails> generalLetterDetailsListValue = firstElement(caseData.getGeneralLetters());

        List<Letter> attachmentLetters = new ArrayList<>();

        if (generalLetterDetailsListValue != null) {

            GeneralLetterDetails letterDetails = generalLetterDetailsListValue.getValue();

            List<ListValue<Document>> documents = letterDetails.getGeneralLetterAttachmentLinks();

            if (!CollectionUtils.isEmpty(documents)) {
                attachmentLetters = mapToLetters(documents, GENERAL_LETTER);
            }

        }
        return attachmentLetters;
    }
}
