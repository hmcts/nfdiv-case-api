package uk.gov.hmcts.divorce.document.print;

import org.apache.commons.io.FilenameUtils;
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
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class BulkPrintServiceTest {
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    @Mock
    private SendLetterApi sendLetterApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private DocumentManagementClient documentManagementClient;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Resource resource;

    @InjectMocks
    private BulkPrintService bulkPrintService;

    @Captor
    private ArgumentCaptor<LetterV3> letterV3ArgumentCaptor;

    @Test
    void shouldReturnLetterIdForValidRequest() throws IOException {
        List<String> solicitorRoles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");

        String solicitorRolesCsv = String.join(",", solicitorRoles);


        String userId = UUID.randomUUID().toString();

        User solicitorUser = solicitorUser(solicitorRoles, userId);

        given(httpServletRequest.getHeader(AUTHORIZATION))
            .willReturn(APP_1_SOL_AUTH_TOKEN);

        given(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN))
            .willReturn(solicitorUser);

        given(authTokenGenerator.generate())
            .willReturn(TEST_SERVICE_AUTH_TOKEN);


        UUID uuid = UUID.randomUUID();
        byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);


        given(sendLetterApi.sendLetter(
            eq(TEST_SERVICE_AUTH_TOKEN),
            isA(LetterV3.class)
        ))
            .willReturn(new SendLetterResponse(
                uuid
            ));

        given(resource.getInputStream())
            .willReturn(new ByteArrayInputStream(firstFile))
            .willReturn(new ByteArrayInputStream(firstFile));

        ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(DIVORCE_APPLICATION);

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());
        given(documentManagementClient.downloadBinary(
            APP_1_SOL_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            solicitorRolesCsv,
            userId,
            documentUuid
        ))
            .willReturn(ResponseEntity.ok(resource));

        ListValue<DivorceDocument> divorceDocumentListValue2 = documentWithType(DIVORCE_APPLICATION);

        final String documentUuid2 = FilenameUtils.getName(
            divorceDocumentListValue2.getValue().getDocumentLink().getUrl());
        given(documentManagementClient.downloadBinary(
            APP_1_SOL_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            solicitorRolesCsv,
            userId,
            documentUuid2
        ))
            .willReturn(ResponseEntity.ok(resource));

        List<Letter> letters = List.of(
            new Letter(
                divorceDocumentListValue.getValue(),
                1
            ),
            new Letter(
                divorceDocumentListValue2.getValue(),
                2
            )
        );

        Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType"
        );

        UUID letterId = bulkPrintService.print(print);
        assertThat(letterId).isEqualTo(uuid);

        verify(sendLetterApi).sendLetter(
            eq(TEST_SERVICE_AUTH_TOKEN),
            letterV3ArgumentCaptor.capture()
        );

        LetterV3 letterV3 = letterV3ArgumentCaptor.getValue();
        assertThat(letterV3.documents)
            .extracting(
                "content",
                "copies")
            .contains(
                tuple(
                    Base64.getEncoder().encodeToString(firstFile),
                    1),
                tuple(
                    Base64.getEncoder().encodeToString(firstFile),
                    2)
            );

        assertThat(letterV3.additionalData)
            .contains(
                entry(LETTER_TYPE_KEY, "letterType"),
                entry(CASE_REFERENCE_NUMBER_KEY, "5678"),
                entry(CASE_IDENTIFIER_KEY, "1234")
            );

        verify(httpServletRequest, times(2))
            .getHeader(AUTHORIZATION);
        verify(idamService, times(2))
            .retrieveUser(APP_1_SOL_AUTH_TOKEN);
        verify(documentManagementClient).downloadBinary(
            APP_1_SOL_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            solicitorRolesCsv,
            userId,
            documentUuid
        );
        verify(authTokenGenerator).generate();

    }

    @Test
    void shouldThrowDocumentDownloadExceptionWhenDocumentCallFails() throws IOException {
        ListValue<DivorceDocument> divorceDocumentListValue = getDivorceDocumentListValue(
            () -> ResponseEntity.ok(resource)
        );

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        List<Letter> letters = List.of(
            new Letter(
                divorceDocumentListValue.getValue(),
                1
            )
        );

        Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType"
        );

        given(resource.getInputStream())
            .willThrow(new IOException("Corrupt data"));

        assertThatThrownBy(() -> bulkPrintService.print(print))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Doc name " + documentUuid);
    }

    @Test
    void shouldThrowDocumentDownloadExceptionWhenResponseEntityIsNull() {
        ListValue<DivorceDocument> divorceDocumentListValue = getDivorceDocumentListValue(() -> null);

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        List<Letter> letters = List.of(
            new Letter(
                divorceDocumentListValue.getValue(),
                1
            )
        );

        Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType"
        );

        assertThatThrownBy(() -> bulkPrintService.print(print))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Resource is invalid " + documentUuid);
    }

    @Test
    void shouldThrowDocumentDownloadExceptionWhenResourceIsNull() {
        ListValue<DivorceDocument> divorceDocumentListValue = getDivorceDocumentListValue(
            () -> ResponseEntity.ok(null)
        );

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        List<Letter> letters = List.of(
            new Letter(
                divorceDocumentListValue.getValue(),
                1
            )
        );

        Print print = new Print(
            letters,
            "1234",
            "5678",
            "letterType"
        );

        assertThatThrownBy(() -> bulkPrintService.print(print))
            .isInstanceOf(InvalidResourceException.class)
            .hasMessage("Resource is invalid " + documentUuid);
    }

    private ListValue<DivorceDocument> getDivorceDocumentListValue(
        Supplier<ResponseEntity<Resource>> responseEntitySupplier) {

        List<String> solicitorRoles = List.of("caseworker-divorce", "caseworker-divorce-solicitor");

        String solicitorRolesCsv = String.join(",", solicitorRoles);


        String userId = UUID.randomUUID().toString();

        User solicitorUser = solicitorUser(solicitorRoles, userId);

        given(httpServletRequest.getHeader(AUTHORIZATION))
            .willReturn(APP_1_SOL_AUTH_TOKEN);

        given(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN))
            .willReturn(solicitorUser);

        given(authTokenGenerator.generate())
            .willReturn(TEST_SERVICE_AUTH_TOKEN);
        ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(DIVORCE_APPLICATION);

        final String documentUuid = FilenameUtils.getName(
            divorceDocumentListValue.getValue().getDocumentLink().getUrl());

        given(documentManagementClient.downloadBinary(
                APP_1_SOL_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                solicitorRolesCsv,
                userId,
                documentUuid
            ))
            .willReturn(responseEntitySupplier.get());

        return divorceDocumentListValue;
    }

    private User solicitorUser(List<String> solicitorRoles, String userId) {
        UserDetails userDetails = UserDetails
            .builder()
            .roles(solicitorRoles)
            .id(userId)
            .build();

        return new User(APP_1_SOL_AUTH_TOKEN, userDetails);
    }
}
