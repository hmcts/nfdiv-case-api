package uk.gov.hmcts.divorce.document.print;

import feign.FeignException;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    public UUID print(final Print print) {
        final String authToken = authTokenGenerator.generate();
        return triggerPrintRequest(print, authToken, documentRequestForPrint(print, authToken));
    }

    public UUID printWithD10Form(final Print print) {
        final String authToken = authTokenGenerator.generate();
        final List<Document> documents = documentRequestForPrint(print, authToken);

        addD10FormTo(documents);

        return triggerPrintRequest(print, authToken, documents);
    }

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
                        RECIPIENTS, getRecipients(documents)
                    )))
                .letterId;
        } catch (FeignException.Conflict e) { // TODO: Remove once bulk print service returns 200 + UUID of request for duplicate requests.
            log.info("Conflict found during call to send letter:" + e.getMessage());
            return UUID.randomUUID();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error while trying to convert document content to MD5: " + e.getMessage());
            return UUID.randomUUID();
        }
    }

    //  getRecipients will generate and return a List of String MD5 hash's for a given list of documents.
    //  Recipients is a unique, repeatable value per request that is used on the bulk print service to identify and
    //  stop duplicate requests from being sent out multiple times.
    private List<String> getRecipients(List<Document> documents) throws NoSuchAlgorithmException {

        List<String> recipients = new ArrayList<>();
        MessageDigest md = MessageDigest.getInstance("MD5");

        for (Document d : documents) {
            //md.update(d.content.getBytes(StandardCharsets.UTF_8));



            // Convert to MD5 String here.
            byte[] digest = md.digest();
            var docMD5 = DatatypeConverter
                .printHexBinary(digest);

            recipients.add(docMD5);
        }

        return recipients;
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
