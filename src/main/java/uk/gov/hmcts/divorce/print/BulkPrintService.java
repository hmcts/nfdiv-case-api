package uk.gov.hmcts.divorce.print;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.print.model.Print;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.model.v3.Document;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@Service
public class BulkPrintService {
    private static final String XEROX_TYPE_PARAMETER = "NFD001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;

    public UUID print(Print print) {
        List<Document> documents = print.getDocuments().stream()
            .map(document ->
                new Document(
                    getEncoder().encodeToString(document.getData()),
                    document.getCount()
                )
            )
            .collect(toList());

        return sendLetterApi.sendLetter(
            authTokenGenerator.generate(),
            new LetterV3(
                XEROX_TYPE_PARAMETER,
                documents,
                Map.of(
                    LETTER_TYPE_KEY, print.getLetterType(),
                    CASE_REFERENCE_NUMBER_KEY, print.getCaseRef(),
                    CASE_IDENTIFIER_KEY, print.getCaseId()
                )))
            .letterId;
    }
}
