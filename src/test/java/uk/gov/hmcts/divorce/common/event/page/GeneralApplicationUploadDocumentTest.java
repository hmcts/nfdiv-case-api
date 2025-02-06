package uk.gov.hmcts.divorce.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getListOfDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationUploadDocumentTest {

    private final GeneralApplicationUploadDocument page = new GeneralApplicationUploadDocument();

    @Test
    public void shouldReturnErrorIfNoDocumentsUploaded() {
        final CaseData caseData = caseData();

        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocuments(null)
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
    public void shouldReturnErrorIfDocumentsUploadedExceedsMaxNumber() {
        final CaseData caseData = caseData();

        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocuments(getListOfDivorceDocumentListValue(11))
            .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);


        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Maximum uploads allowed for event is 10");
    }

    @Test
    public void shouldNotReturnErrorIfDocumentIsUploadedAndWellFormed() {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);

        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocuments(docs)
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

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentLink(null);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);

        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocuments(docs)
            .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);


        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("No document attached to one or more uploads");
    }

    @Test
    public void shouldReturnErrorIfDocumentNameIsNull() {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentFileName(null);
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);

        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocuments(docs)
            .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);


        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Filename is required for uploads");
    }

    @Test
    public void shouldReturnErrorIfDocumentDateIsNull() {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentDateAdded(null);
        docs.get(0).getValue().setDocumentFileName("Testfile");

        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationDocuments(docs)
            .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);


        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Date is a required for uploads");
    }
}
