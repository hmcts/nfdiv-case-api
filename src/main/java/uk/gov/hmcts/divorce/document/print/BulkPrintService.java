package uk.gov.hmcts.divorce.document.print;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.document.CaseDocumentAccessManagement;
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
    private static final String IS_INTERNATIONAL = "isInternational";

    @Autowired
    private SendLetterApi sendLetterApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CaseDocumentAccessManagement documentManagementClient;

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

        return print.getLetters().stream()
            .map(letter ->
                new Document(
                    getEncoder().encodeToString(
                        getDocumentBytes(
                            letter,
                            serviceAuth,
                            userAuth
                        )
                    ),
                    letter.getNumCopiesToPrint()
                )
            )
            .collect(toList());
    }

    private UUID triggerPrintRequest(Print print, String authToken, List<Document> documents) {

        UUID sendLetterUUID = sendLetterApi.sendLetter(
            authToken,
            new LetterV3(
                XEROX_TYPE_PARAMETER,
                documents,
                Map.of(
                    LETTER_TYPE_KEY, print.getLetterType(),
                    CASE_REFERENCE_NUMBER_KEY, print.getCaseRef(),
                    CASE_IDENTIFIER_KEY, print.getCaseId(),
                    RECIPIENTS, print.getRecipients(),
                    IS_INTERNATIONAL, print.getIsInternational()
                )))
            .letterId;

        String logMsg = "Bulk print request sent with letterId: " + sendLetterUUID;
        log.info(Boolean.TRUE.equals(print.getIsInternational()) ? "International " + logMsg : logMsg);

        for (var letter : print.getLetters()) {
            log.info("Sent document {} for case {} in letter {}", getDocument(letter).getFilename(), print.getCaseRef(), sendLetterUUID);
        }

        return sendLetterUUID;
    }

    private uk.gov.hmcts.ccd.sdk.type.Document getDocument(final Letter letter) {
        if (letter.getDivorceDocument() != null) {
            return letter.getDivorceDocument().getDocumentLink();
        } else if (letter.getConfidentialDivorceDocument() != null) {
            return letter.getConfidentialDivorceDocument().getDocumentLink();
        } else if (letter.getDocument() != null) {
            return letter.getDocument();
        } else {
            throw new InvalidResourceException("Invalid document resource");
        }
    }

    private byte[] getDocumentBytes(final Letter letter, final String serviceAuth, final String userAuth) {
        var doc = getDocument(letter);
        var fileName = FilenameUtils.getName(doc.getUrl());
        var resourceResponseEntity = documentManagementClient.downloadBinary(userAuth, serviceAuth, doc);

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
