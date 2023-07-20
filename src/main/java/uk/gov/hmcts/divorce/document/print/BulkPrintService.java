package uk.gov.hmcts.divorce.document.print;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class BulkPrintService {
    private static final String XEROX_TYPE_PARAMETER = "NFDIV001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";
    private static final String RECIPIENTS = "recipients";

    @Autowired
    private SendLetterApi sendLetterApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private DocumentManagementClient documentManagementClient;

    @Autowired
    private IdamService idamService;

    // TODO: NFDIV-3567 - Find all uses of this method. Print now includes a List<String> recipients field and all constructors will need
    //  to be updated to include it.
    public UUID print(final Print print) {
        final String authToken = authTokenGenerator.generate();
        return triggerPrintRequest(print, authToken, documentRequestForPrint(print, authToken));
    }

    // TODO: NFDIV-3567 - Same as above.
    public UUID printWithD10Form(final Print print) {
        final String authToken = authTokenGenerator.generate();
        final List<Document> documents = documentRequestForPrint(print, authToken);

        addD10FormTo(documents);

        return triggerPrintRequest(print, authToken, documents);
    }

    // TODO: NFDIV-3567 - Same as above.
    public UUID printAosRespondentPack(final Print print, final boolean includeD10Document) {
        final String authToken = authTokenGenerator.generate();
        List<Document> documents = documentRequestForPrint(print, authToken);

        if (includeD10Document) {
            addD10FormTo(documents);
        }

        return triggerPrintRequest(print, authToken, documents);
    }

    private void addD10FormTo(final List<Document> documents) {
        final Document d10Document = new Document(getEncoder().encodeToString(loadD10PdfBytes("/D10.pdf")), 1);
        documents.add(d10Document);
    }

    private List<Document> documentRequestForPrint(Print print, String serviceAuth) {

        final var systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();
        final var userAuth = systemUpdateUser.getAuthToken();
        final var userDetails = systemUpdateUser.getUserDetails();
        final var userRoles = String.join(",", userDetails.getRoles());
        final var userId = userDetails.getId();

        return print.getLetters().stream()
            .map(letter ->
                new Document(
                    getEncoder().encodeToString(
                        getDocumentBytes(
                            letter,
                            serviceAuth,
                            userAuth,
                            userRoles,
                            userId
                        )
                    ),
                    letter.getCount()
                )
            )
            .collect(toList());
    }

    // TODO: NFDIV-3567 - New change to include the a 'recipients' key.
    //  This needs to be unique per request & repeatable. Bulk print team will use this signature to check for duplicate
    //  requests and if any duplicates are found within an hour of the original request, they're rejected.
    private UUID triggerPrintRequest(Print print, String authToken, List<Document> documents) {

        try {
            return sendLetterApi.sendLetter(
                authToken,
                new LetterV3(
                    XEROX_TYPE_PARAMETER,
                    documents,
                    Map.of(
                        LETTER_TYPE_KEY, print.getLetterType(),
                        CASE_REFERENCE_NUMBER_KEY, print.getCaseRef(),
                        CASE_IDENTIFIER_KEY, print.getCaseId(),
                        RECIPIENTS, print.getRecipients()
                    )))
                .letterId;

        // TODO: NFDIV-3567 - As per bulk prints team suggestion, for now they return a conflict exception which needs to be handled. At the
        //  end of July when they release their change, they will change to return a 200 response + the ID of the duplicate request.
        //  So for now, handle the exception until it's removed.
        } catch (FeignException.Conflict e) {
            log.info("Duplicate request found: " + e.getMessage());
            return UUID.randomUUID();
        }
    }

    private byte[] getDocumentBytes(final Letter letter,
                                    final String serviceAuth,
                                    final String userAuth,
                                    final String userRoles,
                                    final String userId) {
        String docUrl;

        if (letter.getDivorceDocument() != null) {
            docUrl = letter.getDivorceDocument().getDocumentLink().getUrl();
        } else if (letter.getConfidentialDivorceDocument() != null) {
            docUrl = letter.getConfidentialDivorceDocument().getDocumentLink().getUrl();
        } else {
            throw new InvalidResourceException("Invalid document resource");
        }

        final String fileName = FilenameUtils.getName(docUrl);
        ResponseEntity<Resource> resourceResponseEntity = documentManagementClient.downloadBinary(
            userAuth,
            serviceAuth,
            userRoles,
            userId,
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

    public byte[] loadD10PdfBytes(String resourceName) {
        try {
            return IOUtils.resourceToByteArray(resourceName);
        } catch (IOException e) {
            log.error("Error occurred while loading D10 document from classpath", e);
        }
        return new byte[0];
    }
}
