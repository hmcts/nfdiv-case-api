package uk.gov.hmcts.divorce.document.print;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.document.print.exception.InvalidResourceException;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.model.v3.Document;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
public class BulkPrintService {
    private static final String XEROX_TYPE_PARAMETER = "NFDIV001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    @Autowired
    private SendLetterApi sendLetterApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private IdamService idamService;

    public UUID print(final Print print) {
        final String authToken = authTokenGenerator.generate();
        List<Document> documents = print.getLetters().stream()
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

    private byte[] getDocumentBytes(final String docUrl, final String authToken) {
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
}
