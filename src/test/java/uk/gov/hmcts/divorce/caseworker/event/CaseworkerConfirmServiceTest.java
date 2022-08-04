package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitConfirmService;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerConfirmService.CASEWORKER_CONFIRM_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CaseworkerConfirmServiceTest {

    @Mock
    private SolicitorSubmitConfirmService solicitorSubmitConfirmService;

    @InjectMocks
    private CaseworkerConfirmService caseworkerConfirmService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerConfirmService.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CONFIRM_SERVICE);
    }

    @Test
    void shouldSetDueDateWhenServiceMethodIsCourtService() {
        final CaseData caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.setDueDate(LocalDate.of(2021, 1, 1));
        updatedCaseDetails.setData(caseData);

        when(solicitorSubmitConfirmService.submitConfirmService(caseDetails)).thenReturn(updatedCaseDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerConfirmService.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDueDate()).isEqualTo(LocalDate.of(2021, 1, 1));
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

        when(solicitorSubmitConfirmService.submitConfirmService(caseDetails)).thenReturn(updatedCaseDetails);
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerConfirmService.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDueDate()).isEqualTo(LocalDate.of(2021, 1, 1));
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

        when(solicitorSubmitConfirmService.submitConfirmService(caseDetails)).thenReturn(caseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerConfirmService.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNotEmpty();

        DivorceDocument confirmServiceDoc = response.getData().getDocuments().getDocumentsUploaded().get(0).getValue();

        assertThat(confirmServiceDoc.getDocumentLink().getUrl()).isEqualTo("url");
        assertThat(confirmServiceDoc.getDocumentLink().getFilename()).isEqualTo("filename.pdf");
        assertThat(confirmServiceDoc.getDocumentLink().getBinaryUrl()).isEqualTo("url/binary");
        assertThat(response.getData().getDocuments().getDocumentsUploadedOnConfirmService()).isNull();
    }
}
