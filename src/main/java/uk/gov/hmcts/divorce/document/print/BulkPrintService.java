package uk.gov.hmcts.divorce.document.print;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.document.print.exception.InvalidResourceException;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.model.v3.Document;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
public class BulkPrintService {
    private static final String XEROX_TYPE_PARAMETER = "NFDIV001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    @Autowired
    private SendLetterApi sendLetterApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private IdamService idamService;

    public UUID print(final Print print) {
        final String authToken = authTokenGenerator.generate();
        return triggerPrintRequest(print, authToken, documentRequestForPrint(print, authToken));
    }

    public UUID printAosRespondentPack(final Print print, final boolean includeD10Document) {
        final String authToken = authTokenGenerator.generate();
        List<Document> documents = documentRequestForPrint(print, authToken);

        if (includeD10Document) {
            Document d10Document = new Document(getEncoder().encodeToString(loadD10PdfBytes()), 1);
            documents.add(d10Document);
        }

        return triggerPrintRequest(print, authToken, documents);
    }

    private List<Document> documentRequestForPrint(Print print, String authToken) {
        return print.getLetters().stream()
            .map(letter ->
                new Document(
                    getEncoder().encodeToString(
                        getDocumentBytes(
                            letter,
                            authToken
                        )
                    ),
                    letter.getCount()
                )
            )
            .collect(toList());
    }

    private UUID triggerPrintRequest(Print print, String authToken, List<Document> documents) {
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

    private byte[] getDocumentBytes(final Letter letter, final String authToken) {
        String docUrl = letter.getDivorceDocument() != null
            ? letter.getDivorceDocument().getDocumentLink().getUrl()
            : letter.getScannedDocument().getUrl().getUrl();

        String fileName = FilenameUtils.getName(docUrl);
        final String userAuth = request.getHeader(AUTHORIZATION);
        final var userDetails = idamService.retrieveUser(userAuth).getUserDetails();
        ResponseEntity<Resource> resourceResponseEntity = documentManagementClient.downloadBinary(
            userAuth,
            authToken,
            String.join(",", userDetails.getRoles()),
            userDetails.getId(),
            fileName
        );

        return Optional.ofNullable(resourceResponseEntity)
            .map(ResponseEntity::getBody)
            .map(resource -> {
                try {
                    return resource.getInputStream().readAllBytes();
                } catch (IOException e) {
                    throw new InvalidResourceException("Doc name " + fileName, e);
                }
            })
            .orElseThrow(() -> new InvalidResourceException("Resource is invalid " + fileName));
    }

    public byte[] loadD10PdfBytes() {
        Path path = Paths.get("src/main/resources/D10.pdf");
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("Error occurred while loading D10 document from classpath", e);
        }
        return null;
    }
}
