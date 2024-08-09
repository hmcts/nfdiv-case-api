package uk.gov.hmcts.divorce.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.event.page.GeneralApplicationUploadDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationUploadDocumentTest {

    private final GeneralApplicationUploadDocument page = new GeneralApplicationUploadDocument();

    @Test
    public void shouldNotReturnErrorIfDocumentIsUploaded() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocument(
                DivorceDocument.builder()
                    .documentLink(Document.builder().build())
                    .build()
            )
            .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    public void shouldReturnErrorIfDocumentIsNull() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocument(null)
            .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);


        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please upload a document in order to continue");
    }

    @Test
    public void shouldReturnErrorIfDocumentIsNotUploaded() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocument(DivorceDocument.builder().build())
            .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);


        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please upload a document in order to continue");
    }
}
