package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRemoveDocument.CASEWORKER_REMOVE_DOCUMENT;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class CaseworkerRemoveDocumentFT extends FunctionalTestSuite {

    private static final String CASEWORKER_REMOVE_DOCUMENT_BEFORE_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-remove-document-before-data.json";

    private static final String CASEWORKER_REMOVE_DOCUMENT_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-remove-document-data.json";

    private static final String CASEWORKER_REMOVE_SCANNED_DOCUMENT_BEFORE_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-remove-scanned-document-before-data.json";

    private static final String CASEWORKER_REMOVE_SCANNED_DOCUMENT_REQUEST =
        "classpath:request/casedata/ccd-callback-caseworker-remove-document-data.json";

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldRemoveDocument() throws Exception {

        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(CASEWORKER_REMOVE_DOCUMENT)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .data(caseData(CASEWORKER_REMOVE_DOCUMENT_BEFORE_REQUEST))
                    .caseTypeId(getCaseType())
                    .state("Submitted")
                    .build()
            )
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(caseData(CASEWORKER_REMOVE_DOCUMENT_REQUEST))
                    .caseTypeId(getCaseType())
                    .state("Submitted")
                    .build()
            )
            .build();

        final Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldRemoveScannedDocument() throws Exception {

        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(CASEWORKER_REMOVE_DOCUMENT)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .data(caseData(CASEWORKER_REMOVE_SCANNED_DOCUMENT_BEFORE_REQUEST))
                    .caseTypeId(getCaseType())
                    .state("Submitted")
                    .build()
            )
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(caseData(CASEWORKER_REMOVE_SCANNED_DOCUMENT_REQUEST))
                    .caseTypeId(getCaseType())
                    .state("Submitted")
                    .build()
            )
            .build();

        final Response response = triggerCallback(request, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
