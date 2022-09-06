package uk.gov.hmcts.divorce.common.service;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.service.ConfirmService.DOCUMENTS_NOT_UPLOADED_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
public class ConfirmServiceTest {

    @InjectMocks
    private ConfirmService confirmService;

    @Test
    public void shouldAddValidationErrorWhenServiceProcessedByProcessServerAndNoDocumentsUploaded() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .solicitorService(SolicitorService.builder()
                    .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
                    .build())
                .build())
            .documents(CaseDocuments.builder().build())
            .build();

        List<String> errors = confirmService.validateConfirmService(caseData);

        assertThat(errors).isNotEmpty();
        assertThat(errors).contains(DOCUMENTS_NOT_UPLOADED_ERROR);
    }

    @Test
    public void shouldNotAddValidationErrorWhenServiceProcessedByProcessServerAndDocumentsAreUploaded() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .solicitorService(SolicitorService.builder()
                    .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
                    .build())
                .build())
            .documents(CaseDocuments.builder()
                .documentsUploadedOnConfirmService(List.of(documentWithType(OTHER)))
                .build())
            .build();

        List<String> errors = confirmService.validateConfirmService(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldNotAddValidationErrorWhenServiceNotProcessedByProcessServer() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .solicitorService(SolicitorService.builder().build())
                .build())
            .build();

        List<String> errors = confirmService.validateConfirmService(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnErrorResponseWhenThereAreValidationErrors() {
        List<String> validationErrors = Lists.newArrayList(DOCUMENTS_NOT_UPLOADED_ERROR);

        AboutToStartOrSubmitResponse<CaseData, State> response = confirmService.getErrorResponse(
            CaseDetails.<CaseData, State>builder().build(), validationErrors);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).contains(DOCUMENTS_NOT_UPLOADED_ERROR);
    }

    @Test
    public void shouldAddAnyConfirmServiceAttachmentsToDocumentsUploadedList() {
        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        final ListValue<DivorceDocument> confirmServiceAttachments = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentLink(new Document("url", "filename.pdf", "url/binary"))
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsUploaded(new ArrayList<>())
            .build());

        caseData.getDocuments().setDocumentsUploadedOnConfirmService(Lists.newArrayList(confirmServiceAttachments));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        confirmService.addToDocumentsUploaded(caseDetails);

        CaseDocuments documents = caseDetails.getData().getDocuments();

        assertThat(documents.getDocumentsUploaded()).isNotEmpty();

        DivorceDocument confirmServiceDoc = documents.getDocumentsUploaded().get(0).getValue();

        assertThat(confirmServiceDoc.getDocumentLink().getUrl()).isEqualTo("url");
        assertThat(confirmServiceDoc.getDocumentLink().getFilename()).isEqualTo("filename.pdf");
        assertThat(confirmServiceDoc.getDocumentLink().getBinaryUrl()).isEqualTo("url/binary");
        assertThat(documents.getDocumentsUploadedOnConfirmService()).isNull();
    }

    @Test
    public void shouldAddAnyConfirmServiceAttachmentsToDocumentsUploadedListWhenDocumentsUploadedIsNull() {
        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        final ListValue<DivorceDocument> confirmServiceAttachments = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentLink(new Document("url", "filename.pdf", "url/binary"))
                .build())
            .build();

        caseData.getDocuments().setDocumentsUploadedOnConfirmService(Lists.newArrayList(confirmServiceAttachments));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        confirmService.addToDocumentsUploaded(caseDetails);

        CaseDocuments documents = caseDetails.getData().getDocuments();

        assertThat(documents.getDocumentsUploaded()).isNotEmpty();

        DivorceDocument confirmServiceDoc = documents.getDocumentsUploaded().get(0).getValue();

        assertThat(confirmServiceDoc.getDocumentLink().getUrl()).isEqualTo("url");
        assertThat(confirmServiceDoc.getDocumentLink().getFilename()).isEqualTo("filename.pdf");
        assertThat(confirmServiceDoc.getDocumentLink().getBinaryUrl()).isEqualTo("url/binary");
        assertThat(documents.getDocumentsUploadedOnConfirmService()).isNull();
    }
}
