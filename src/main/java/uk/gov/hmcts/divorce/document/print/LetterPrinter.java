package uk.gov.hmcts.divorce.document.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.LetterPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack.LETTER_TYPE_GENERAL_LETTER;
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

        var currentPacks = Optional.ofNullable(caseData.getDocuments().getLetterPacks()).orElse(new ArrayList<>());
        var documents = letters.stream().map(this::documentFromLetter).map(this::toListValue).toList();
        var letterPack = LetterPack.builder().letters(documents)
            .recipientAddress(applicant.getCorrespondenceAddressWithoutConfidentialCheck())
            .build();
        currentPacks.add(ListValue.<LetterPack>builder().id(UUID.randomUUID().toString()).value(letterPack).build());
        caseData.getDocuments().setLetterPacks(currentPacks);

        if (!isEmpty(letters) && letters.size() == documentPackInfo.documentPack().size()) {

            if (LETTER_TYPE_GENERAL_LETTER.equals(letterName)) {
                sendGeneralLetterWithAttachments(caseData, caseId.toString(), letterName, letters);
            } else {
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
            }
        } else {
            throw new IllegalArgumentException(
                "%s letter pack has missing documents. Expected documents with type %s for case %s".formatted(
                    letterName,
                    documentPackInfo.documentPack().keySet(),
                    caseId)
            );
        }
    }

    private void sendGeneralLetterWithAttachments(CaseData caseData, String caseId, String letterName, List<Letter> letters) {

        GeneralLetter generalLetter = caseData.getGeneralLetter();
        if (generalLetter != null) {
            letters.addAll(addAnyAttachmentsToPackForGeneralLetter(generalLetter));

            GeneralParties parties = generalLetter.getGeneralLetterParties();
            var recipientName = switch (parties) {
                case RESPONDENT -> caseData.getApplicant2().getFullName();
                case APPLICANT -> caseData.getApplicant1().getFullName();
                case OTHER -> caseData.getGeneralLetter().getOtherRecipientName();
            };

            final Print print = new Print(
                letters,
                caseId,
                caseId,
                letterName,
                recipientName
            );

            final UUID letterId = bulkPrintService.print(print);
            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            throw new IllegalArgumentException(
                "Error when sending general letter: GeneralLetter is null for case %s".formatted(caseId)
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

    private List<Letter> addAnyAttachmentsToPackForGeneralLetter(final GeneralLetter generalLetter) {

        final AtomicInteger letterIndex = new AtomicInteger();

        return Optional.ofNullable(generalLetter.getGeneralLetterAttachments())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(ListValue::getValue)
            .map(document -> new Letter(DivorceDocument.builder()
                .documentType(GENERAL_LETTER)
                .documentFileName(document.getDocumentFileName())
                .documentLink(document.getDocumentLink())
                .build(), letterIndex.incrementAndGet()))
            .collect(toList());
    }
}
