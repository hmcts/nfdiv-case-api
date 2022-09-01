package uk.gov.hmcts.divorce.solicitor.event;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.ConfirmService;
import uk.gov.hmcts.divorce.common.service.SubmitConfirmService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.service.ConfirmService.DOCUMENTS_NOT_UPLOADED_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorConfirmService.SOLICITOR_CONFIRM_SERVICE;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorConfirmService.SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SolicitorConfirmServiceTest {

    @Mock
    private SubmitConfirmService submitConfirmService;

    @Mock
    private ConfirmService confirmService;

    @InjectMocks
    private SolicitorConfirmService solicitorConfirmService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorConfirmService.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_CONFIRM_SERVICE);
    }

    @Test
    void shouldSetDueDateWhenServiceMethodIsSolicitorService() {
        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.setDueDate(LocalDate.of(2021, 1, 1));
        updatedCaseDetails.setData(caseData);

        when(submitConfirmService.submitConfirmService(caseDetails)).thenReturn(updatedCaseDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorConfirmService.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDueDate()).isEqualTo(LocalDate.of(2021, 1, 1));

        verify(confirmService).addToDocumentsUploaded(caseDetails);
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

        when(submitConfirmService.submitConfirmService(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorConfirmService.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();

        verify(confirmService).addToDocumentsUploaded(caseDetails);
    }

    @Test
    void shouldThrowErrorWhenServiceMethodIsNotSolicitorService() {
        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.setDueDate(LocalDate.of(2021, 1, 1));
        updatedCaseDetails.setData(caseData);

        List<String> validationErrors = Lists.newArrayList(SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR);
        when(confirmService.validateConfirmService(caseData)).thenReturn(new ArrayList<>());
        when(confirmService.getErrorResponse(caseDetails, validationErrors)).thenReturn(
            AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseDetails.getData())
                .errors(validationErrors)
                .state(caseDetails.getState())
                .build()
        );

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorConfirmService.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).contains(SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR);

        verifyNoMoreInteractions(confirmService);
    }

    @Test
    public void shouldThrowValidationErrorWhenProcessedByProcessServerButDocumentsNotUploaded() {
        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setSolicitorService(SolicitorService.builder()
            .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
            .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        List<String> validationErrors = Lists.newArrayList(DOCUMENTS_NOT_UPLOADED_ERROR);
        when(confirmService.validateConfirmService(caseData)).thenReturn(validationErrors);
        when(confirmService.getErrorResponse(caseDetails, validationErrors)).thenReturn(
            AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseDetails.getData())
                .errors(validationErrors)
                .state(caseDetails.getState())
                .build()
        );

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorConfirmService.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).contains(DOCUMENTS_NOT_UPLOADED_ERROR);

        verifyNoMoreInteractions(confirmService);
    }

    @Test
    public void shouldNotThrowValidationErrorWhenProcessedByProcessServerButDocumentsUploaded() {
        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setSolicitorService(SolicitorService.builder()
                .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
            .build());

        final ListValue<DivorceDocument> confirmServiceAttachments = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentLink(new Document("url", "filename.pdf", "url/binary"))
                .build())
            .build();

        caseData.getDocuments().setDocumentsUploadedOnConfirmService(Lists.newArrayList(confirmServiceAttachments));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorConfirmService.midEvent(caseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }
}
