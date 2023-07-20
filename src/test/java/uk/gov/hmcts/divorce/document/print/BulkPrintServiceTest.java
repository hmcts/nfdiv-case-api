package uk.gov.hmcts.divorce.document.print;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.DocumentManagementClient;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.exception.InvalidResourceException;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Base64.getEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.confidentialDocumentWithType;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class BulkPrintServiceTest {
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";
    private static final String RECIPIENTS = "recipients";

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private DocumentManagementClient documentManagementClient;

    @Mock
    private Resource resource;

    @InjectMocks
    private BulkPrintService bulkPrintService;

    @Captor
    private ArgumentCaptor<LetterV3> letterV3ArgumentCaptor;

    @Test
    void shouldReturnLetterIdForValidRequest() throws IOException {
        final List<String> roles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");
        final String rolesCsv = String.join(",", roles);
        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = solicitorUser(roles, userId);

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);
        given(authTokenGenerator.generate()).willReturn(TEST_SERVICE_AUTH_TOKEN);

        final UUID uuid = UUID.randomUUID();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        given(sendLetterApi.sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), isA(LetterV3.class)))
            .willReturn(new SendLetterResponse(uuid));

        given(resource.getInputStream())
            .willReturn(new ByteArrayInputStream(firstFile))
            .willReturn(new ByteArrayInputStream(firstFile));

        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(APPLICATION);

        final String documentUuid = FilenameUtils.getName(divorceDocumentListValue.getValue().getDocumentLink().getUrl());
        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid))
            .willReturn(ResponseEntity.ok(resource));

        final ListValue<DivorceDocument> divorceDocumentListValue2 = documentWithType(APPLICATION);

        final String documentUuid2 = FilenameUtils.getName(
            divorceDocumentListValue2.getValue().getDocumentLink().getUrl());
        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid2))
            .willReturn(ResponseEntity.ok(resource));

        final List<Letter> letters = List.of(
            new Letter(divorceDocumentListValue.getValue(), 1),
            new Letter(divorceDocumentListValue2.getValue(), 2)
        );

        final Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        final UUID letterId = bulkPrintService.print(print);
        assertThat(letterId).isEqualTo(uuid);

        verify(sendLetterApi).sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), letterV3ArgumentCaptor.capture());

        final LetterV3 letterV3 = letterV3ArgumentCaptor.getValue();
        assertThat(letterV3.documents)
            .extracting("content", "copies")
            .contains(
                tuple(getEncoder().encodeToString(firstFile), 1),
                tuple(getEncoder().encodeToString(firstFile), 2)
            );

        assertThat(letterV3.additionalData)
            .contains(
                entry(LETTER_TYPE_KEY, "letterType"),
                entry(CASE_REFERENCE_NUMBER_KEY, "5678"),
                entry(CASE_IDENTIFIER_KEY, "1234"),
                entry(RECIPIENTS, List.of("Test User", "letterType"))
            );

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(documentManagementClient).downloadBinary(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            rolesCsv,
            userId,
            documentUuid
        );
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldReturnLetterIdForAosRespondentPackWithoutD10DocumentsWhenPrintRequestIsInvoked() throws IOException {
        final List<String> roles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");
        final String rolesCsv = String.join(",", roles);
        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = solicitorUser(roles, userId);

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);
        given(authTokenGenerator.generate()).willReturn(TEST_SERVICE_AUTH_TOKEN);

        final UUID uuid = UUID.randomUUID();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        given(sendLetterApi.sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), isA(LetterV3.class)))
            .willReturn(new SendLetterResponse(uuid));

        given(resource.getInputStream())
            .willReturn(new ByteArrayInputStream(firstFile))
            .willReturn(new ByteArrayInputStream(firstFile));

        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(APPLICATION);

        final String documentUuid = FilenameUtils.getName(divorceDocumentListValue.getValue().getDocumentLink().getUrl());
        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid))
            .willReturn(ResponseEntity.ok(resource));

        final ListValue<DivorceDocument> divorceDocumentListValue2 = documentWithType(APPLICATION);

        final String documentUuid2 = FilenameUtils.getName(divorceDocumentListValue2.getValue().getDocumentLink().getUrl());
        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid2))
            .willReturn(ResponseEntity.ok(resource));

        final List<Letter> letters = List.of(
            new Letter(divorceDocumentListValue.getValue(), 1),
            new Letter(divorceDocumentListValue2.getValue(), 2)
        );

        final Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        final UUID letterId = bulkPrintService.printAosRespondentPack(print, false);
        assertThat(letterId).isEqualTo(uuid);

        verify(sendLetterApi).sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), letterV3ArgumentCaptor.capture());

        final LetterV3 letterV3 = letterV3ArgumentCaptor.getValue();
        assertThat(letterV3.documents)
            .extracting("content", "copies")
            .contains(
                tuple(getEncoder().encodeToString(firstFile), 1),
                tuple(getEncoder().encodeToString(firstFile), 2)
            );

        assertThat(letterV3.additionalData)
            .contains(
                entry(LETTER_TYPE_KEY, "letterType"),
                entry(CASE_REFERENCE_NUMBER_KEY, "5678"),
                entry(CASE_IDENTIFIER_KEY, "1234"),
                entry(RECIPIENTS, List.of("Test User", "letterType"))
            );

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(documentManagementClient).downloadBinary(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            rolesCsv,
            userId,
            documentUuid
        );
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldReturnLetterIdForAosRespondentPackWithD10DocumentsWhenPrintRequestIsInvoked() throws IOException {
        final List<String> roles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");
        final String rolesCsv = String.join(",", roles);
        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = solicitorUser(roles, userId);

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);
        given(authTokenGenerator.generate()).willReturn(TEST_SERVICE_AUTH_TOKEN);

        final UUID uuid = UUID.randomUUID();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
        final byte[] d10PdfBytes = bulkPrintService.loadD10PdfBytes("/D10.pdf");

        given(sendLetterApi.sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), isA(LetterV3.class)))
            .willReturn(new SendLetterResponse(uuid));

        given(resource.getInputStream())
            .willReturn(new ByteArrayInputStream(firstFile))
            .willReturn(new ByteArrayInputStream(firstFile));

        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(APPLICATION);

        final String documentUuid = FilenameUtils.getName(divorceDocumentListValue.getValue().getDocumentLink().getUrl());
        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid))
            .willReturn(ResponseEntity.ok(resource));

        final ListValue<DivorceDocument> divorceDocumentListValue2 = documentWithType(APPLICATION);

        final String documentUuid2 = FilenameUtils.getName(divorceDocumentListValue2.getValue().getDocumentLink().getUrl());
        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid2))
            .willReturn(ResponseEntity.ok(resource));

        final List<Letter> letters = List.of(
            new Letter(divorceDocumentListValue.getValue(), 1),
            new Letter(divorceDocumentListValue2.getValue(), 2)
        );

        Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        final UUID letterId = bulkPrintService.printAosRespondentPack(print, true);
        assertThat(letterId).isEqualTo(uuid);

        verify(sendLetterApi).sendLetter(
            eq(TEST_SERVICE_AUTH_TOKEN),
            letterV3ArgumentCaptor.capture()
        );

        final LetterV3 letterV3 = letterV3ArgumentCaptor.getValue();
        assertThat(letterV3.documents)
            .extracting("content", "copies")
            .contains(
                tuple(getEncoder().encodeToString(firstFile), 1),
                tuple(getEncoder().encodeToString(firstFile), 2),
                tuple(getEncoder().encodeToString(d10PdfBytes), 1)
            );

        assertThat(letterV3.additionalData)
            .contains(
                entry(LETTER_TYPE_KEY, "letterType"),
                entry(CASE_REFERENCE_NUMBER_KEY, "5678"),
                entry(CASE_IDENTIFIER_KEY, "1234"),
                entry(RECIPIENTS, List.of("Test User", "letterType"))
            );

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(documentManagementClient).downloadBinary(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            rolesCsv,
            userId,
            documentUuid
        );
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldReturnLetterIdForAosPackWithD10DocumentsWhenPrintRequestIsInvoked() throws IOException {
        final List<String> roles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");
        final String rolesCsv = String.join(",", roles);
        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = solicitorUser(roles, userId);

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);
        given(authTokenGenerator.generate()).willReturn(TEST_SERVICE_AUTH_TOKEN);

        final UUID uuid = UUID.randomUUID();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
        final byte[] d10PdfBytes = bulkPrintService.loadD10PdfBytes("/D10.pdf");

        given(sendLetterApi.sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), isA(LetterV3.class)))
            .willReturn(new SendLetterResponse(uuid));

        given(resource.getInputStream())
            .willReturn(new ByteArrayInputStream(firstFile))
            .willReturn(new ByteArrayInputStream(firstFile));

        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(APPLICATION);
        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid))
            .willReturn(ResponseEntity.ok(resource));

        final ListValue<DivorceDocument> divorceDocumentListValue2 = documentWithType(APPLICATION);

        final String documentUuid2 = FilenameUtils.getName(
            divorceDocumentListValue2.getValue().getDocumentLink().getUrl());
        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid2))
            .willReturn(ResponseEntity.ok(resource));

        final List<Letter> letters = List.of(
            new Letter(divorceDocumentListValue.getValue(), 1),
            new Letter(divorceDocumentListValue2.getValue(), 2)
        );

        final Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        final UUID letterId = bulkPrintService.printWithD10Form(print);
        assertThat(letterId).isEqualTo(uuid);

        verify(sendLetterApi).sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), letterV3ArgumentCaptor.capture());

        final LetterV3 letterV3 = letterV3ArgumentCaptor.getValue();
        assertThat(letterV3.documents)
            .extracting("content", "copies")
            .contains(
                tuple(getEncoder().encodeToString(firstFile), 1),
                tuple(getEncoder().encodeToString(firstFile), 2),
                tuple(getEncoder().encodeToString(d10PdfBytes), 1)
            );

        assertThat(letterV3.additionalData)
            .contains(
                entry(LETTER_TYPE_KEY, "letterType"),
                entry(CASE_REFERENCE_NUMBER_KEY, "5678"),
                entry(CASE_IDENTIFIER_KEY, "1234"),
                entry(RECIPIENTS, List.of("Test User", "letterType"))
            );

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(documentManagementClient).downloadBinary(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            rolesCsv,
            userId,
            documentUuid
        );
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldThrowDocumentDownloadExceptionWhenDocumentCallFails() throws IOException {
        final ListValue<DivorceDocument> divorceDocumentListValue = getDivorceDocumentListValue(
            () -> ResponseEntity.ok(resource)
        );

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        final List<Letter> letters = List.of(
            new Letter(
                divorceDocumentListValue.getValue(),
                1
            )
        );

        final Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        given(resource.getInputStream()).willThrow(new IOException("Corrupt data"));

        assertThatThrownBy(() -> bulkPrintService.print(print))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Doc name " + documentUuid);
    }

    @Test
    void shouldThrowDocumentDownloadExceptionWhenResponseEntityIsNull() {
        ListValue<DivorceDocument> divorceDocumentListValue = getDivorceDocumentListValue(() -> null);

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        final List<Letter> letters = List.of(
            new Letter(
                divorceDocumentListValue.getValue(),
                1
            )
        );

        final Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        assertThatThrownBy(() -> bulkPrintService.print(print))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Resource is invalid " + documentUuid);
    }

    @Test
    void shouldThrowDocumentDownloadExceptionWhenResourceIsNull() {
        final ListValue<DivorceDocument> divorceDocumentListValue = getDivorceDocumentListValue(
            () -> ResponseEntity.ok(null)
        );

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        final List<Letter> letters = List.of(
            new Letter(
                divorceDocumentListValue.getValue(),
                1
            )
        );

        final Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        assertThatThrownBy(() -> bulkPrintService.print(print))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Resource is invalid " + documentUuid);
    }

    @Test
    void shouldReturnEmptyByteArrayWhenLoadPdfIsInvokedWithNonExistingResource() {
        assertThat(bulkPrintService.loadD10PdfBytes("nonexistent.pdf")).isEmpty();
    }

    @Test
    void shouldLoadD10DocumentSuccessfully() throws Exception {
        final PDDocument d10 = PDDocument.load(bulkPrintService.loadD10PdfBytes("/D10.pdf"));
        assertThat(new PDFTextStripper().getText(d10))
            .contains("D10 Respond to a divorce, dissolution or (judicial) separation application");
    }

    @Test
    void shouldReturnLetterIdForValidRequestForConfidentialDocuments() throws IOException {
        final List<String> roles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");
        final String rolesCsv = String.join(",", roles);
        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = solicitorUser(roles, userId);

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);
        given(authTokenGenerator.generate()).willReturn(TEST_SERVICE_AUTH_TOKEN);

        final UUID uuid = UUID.randomUUID();
        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        given(sendLetterApi.sendLetter(eq(TEST_SERVICE_AUTH_TOKEN), isA(LetterV3.class)))
            .willReturn(new SendLetterResponse(uuid));

        given(resource.getInputStream())
            .willReturn(new ByteArrayInputStream(firstFile))
            .willReturn(new ByteArrayInputStream(firstFile));

        final ListValue<ConfidentialDivorceDocument> divorceDocumentListValue = confidentialDocumentWithType(
            ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1);

        final String documentUuid = FilenameUtils.getName(divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid))
            .willReturn(ResponseEntity.ok(resource));

        final List<Letter> letters = List.of(
            new Letter(divorceDocumentListValue.getValue(), 1)
        );

        final Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        final UUID letterId = bulkPrintService.print(print);
        assertThat(letterId).isEqualTo(uuid);

        verify(sendLetterApi).sendLetter(
            eq(TEST_SERVICE_AUTH_TOKEN),
            letterV3ArgumentCaptor.capture()
        );

        final LetterV3 letterV3 = letterV3ArgumentCaptor.getValue();
        assertThat(letterV3.documents)
            .extracting("content", "copies")
            .contains(
                tuple(getEncoder().encodeToString(firstFile), 1)
            );

        assertThat(letterV3.additionalData)
            .contains(
                entry(LETTER_TYPE_KEY, "letterType"),
                entry(CASE_REFERENCE_NUMBER_KEY, "5678"),
                entry(CASE_IDENTIFIER_KEY, "1234"),
                entry(RECIPIENTS, List.of("Test User", "letterType"))
            );

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(documentManagementClient).downloadBinary(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            rolesCsv,
            userId,
            documentUuid
        );
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldThrowExceptionWhenDocumentResourceIsNull() {
        final List<String> roles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");
        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = solicitorUser(roles, userId);

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);

        ListValue<DivorceDocument> divorceDocumentListValue = ListValue.<DivorceDocument>builder()
            .value(null)
            .id("1")
            .build();

        List<Letter> letters = List.of(new Letter(divorceDocumentListValue.getValue(), 1));

        Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType",
            List.of("Test User", "letterType")
        );

        assertThatThrownBy(() -> bulkPrintService.print(print))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Invalid document resource");
    }

    private ListValue<DivorceDocument> getDivorceDocumentListValue(
        final Supplier<ResponseEntity<Resource>> responseEntitySupplier) {

        final List<String> roles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");
        final String rolesCsv = String.join(",", roles);
        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = solicitorUser(roles, userId);

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);
        given(authTokenGenerator.generate()).willReturn(TEST_SERVICE_AUTH_TOKEN);

        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(APPLICATION);

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, rolesCsv, userId, documentUuid))
            .willReturn(responseEntitySupplier.get());

        return divorceDocumentListValue;
    }

    private User solicitorUser(final List<String> roles, final String userId) {
        UserDetails userDetails = UserDetails
            .builder()
            .roles(roles)
            .id(userId)
            .build();

        return new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);
    }
}
