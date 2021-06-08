package uk.gov.hmcts.divorce.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.print.model.Document;
import uk.gov.hmcts.divorce.print.model.Print;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.model.v3.LetterV3;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BulkPrintServiceTest {
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    @Mock
    private SendLetterApi sendLetterApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private BulkPrintService bulkPrintService;

    @Captor
    ArgumentCaptor<LetterV3> letterV3ArgumentCaptor;

    @Test
    void shouldReturnLetterIdForValidRequest() {
        UUID uuid = UUID.randomUUID();
        String authToken = "authToken";
        byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);
        byte[] secondFile = "data from file 2".getBytes(StandardCharsets.UTF_8);

        given(authTokenGenerator.generate())
            .willReturn(authToken);

        given(sendLetterApi.sendLetter(
                eq(authToken),
                isA(LetterV3.class)
            ))
            .willReturn(new SendLetterResponse(
                uuid
            ));

        List<Document> documents = List.of(
            new Document(
                "fileName1",
                firstFile,
                1
            ),
            new Document(
                "fileName2",
                secondFile,
                2
            )
        );

        Print print = new Print(
            documents,
            "1234",
            "5678",
            "letterType"
            );

        UUID letterId = bulkPrintService.print(print);
        assertThat(letterId).isEqualTo(uuid);

        verify(sendLetterApi).sendLetter(
            eq(authToken),
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
                    Base64.getEncoder().encodeToString(secondFile),
                    2)
            );

        assertThat(letterV3.additionalData)
            .contains(
                entry(LETTER_TYPE_KEY, "letterType"),
                entry(CASE_REFERENCE_NUMBER_KEY, "5678"),
                entry(CASE_IDENTIFIER_KEY, "1234")
            );
    }
}
