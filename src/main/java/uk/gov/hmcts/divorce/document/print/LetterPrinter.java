package uk.gov.hmcts.divorce.document.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.LetterPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

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

        var currentPacks = Optional.ofNullable(caseData.getDocuments().getLetterPacks()).orElse(new ArrayList<>());
        var documents = letters.stream().map(this::documentFromLetter).map(this::toListValue).toList();
        var letterPack = LetterPack.builder().letters(documents).recipientAddress(applicant.getAddress()).build();
        currentPacks.add(ListValue.<LetterPack>builder().id(UUID.randomUUID().toString()).value(letterPack).build());

        if (!isEmpty(letters) && letters.size() == documentPackInfo.documentPack().size()) {

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

    private ListValue<Document> toListValue(Document document) {
        return ListValue.<Document>builder()
            .value(document)
            .id(UUID.randomUUID().toString())
            .build();
    }

    private Document documentFromLetter(Letter letter) {
        if (letter.getConfidentialDivorceDocument() != null) {
            return letter.getConfidentialDivorceDocument().getDocumentLink();
        } else if (letter.getDivorceDocument() != null) {
            return letter.getDivorceDocument().getDocumentLink();
        } else {
            return letter.getDocument();
        }
    }
}
