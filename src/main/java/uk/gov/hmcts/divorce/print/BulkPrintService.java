package uk.gov.hmcts.divorce.print;

import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.print.exception.BulkPrintException;
import uk.gov.hmcts.divorce.print.model.Print;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.model.v3.Document;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@AllArgsConstructor
@Service
public class BulkPrintService {
    private static final String XEROX_TYPE_PARAMETER = "NFD001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest request;
    private final DocumentManagementClient documentManagementClient;
    private final IdamService idamService;

    public UUID print(final Print print) {
        final var authToken = authTokenGenerator.generate();
        var documents = print.getLetters().stream()
            .map(document ->
                new Document(
                    getEncoder().encodeToString(
                        getDocumentBytes(
                            document.getDivorceDocument().getDocumentLink().getUrl(),
                            authToken
                        )
                    ),
                    document.getCount()
                )
            )
            .collect(toList());

        return sendLetterApi.sendLetter(
            authToken,
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

    private byte[] getDocumentBytes(final String docUrl, final String authToken)  {
        final var userAuth = request.getHeader(AUTHORIZATION);
        final var userDetails = idamService.retrieveUser(userAuth).getUserDetails();
        var resourceResponseEntity = documentManagementClient.downloadBinary(
            userAuth,
            authToken,
            String.join(",", userDetails.getRoles()),
            userDetails.getId(),
            FilenameUtils.getName(docUrl)
        );
        try {
            return resourceResponseEntity.getBody().getInputStream().readAllBytes();
        } catch (Exception e) {
            throw new BulkPrintException("Doc url " + docUrl, e);
        }
    }
}
