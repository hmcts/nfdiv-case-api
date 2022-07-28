package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_AS_SERVICE_GRANTED;


class AlternativeServiceOutcomeTest {

    @Test
    void shouldReturnCertificateOfServiceDocumentLinkIfPresent() {

        final AlternativeServiceOutcome alternativeServiceOutcome = AlternativeServiceOutcome.builder().build();
        assertThat(alternativeServiceOutcome.getCertificateOfServiceDocumentLink()).isEqualTo(Optional.empty());

        final Document documentLink = Document.builder()
            .filename("deemedDocument.pdf")
            .build();

        alternativeServiceOutcome.setCertificateOfServiceDocument(DivorceDocument
            .builder()
            .documentLink(documentLink)
            .documentFileName("deemedDocument-12345.pdf")
            .documentType(DEEMED_AS_SERVICE_GRANTED)
            .build());

        assertThat(alternativeServiceOutcome.getCertificateOfServiceDocumentLink()).isEqualTo(Optional.of(documentLink));
    }

    @Test
    void shouldReturnBooleanForServiceApplicationGranted() {

        final AlternativeServiceOutcome alternativeServiceOutcome = AlternativeServiceOutcome.builder().build();
        assertThat(alternativeServiceOutcome.hasServiceApplicationBeenGranted()).isFalse();

        alternativeServiceOutcome.setServiceApplicationGranted(NO);
        assertThat(alternativeServiceOutcome.hasServiceApplicationBeenGranted()).isFalse();

        alternativeServiceOutcome.setServiceApplicationGranted(YES);
        assertThat(alternativeServiceOutcome.hasServiceApplicationBeenGranted()).isTrue();
    }

    @Test
    void shouldReturnBooleanForSuccessfulServedByBailiff() {

        final AlternativeServiceOutcome alternativeServiceOutcome = AlternativeServiceOutcome.builder().build();
        assertThat(alternativeServiceOutcome.hasBeenSuccessfullyServedByBailiff()).isFalse();

        alternativeServiceOutcome.setSuccessfulServedByBailiff(NO);
        assertThat(alternativeServiceOutcome.hasBeenSuccessfullyServedByBailiff()).isFalse();

        alternativeServiceOutcome.setSuccessfulServedByBailiff(YES);
        assertThat(alternativeServiceOutcome.hasBeenSuccessfullyServedByBailiff()).isTrue();
    }
}