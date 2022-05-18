package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.Bailiff;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CertificateOfServiceContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueBailiffPack.CASEWORKER_ISSUE_BAILIFF_PACK;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_SERVICE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueBailiffPackTest {

    private static final int FIRST = 0;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CertificateOfServiceContent certificateOfServiceContent;

    @InjectMocks
    private CaseworkerIssueBailiffPack issueBailiffPack;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        issueBailiffPack.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_BAILIFF_PACK);
    }

    @Test
    void shouldUpdateCaseWithCertificateOfServiceDocumentWhenAboutToSubmitCallbackIsTriggered() {
        final CaseData caseData = caseData();
        caseData.setDocuments(CaseDocuments.builder().build());
        caseData.getDocuments().setDocumentsGenerated(new ArrayList<>());

        final Map<String, Object> templateContent = new HashMap<>();

        when(certificateOfServiceContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        Document certificateOfServiceDocument = new Document(
            documentUrl,
            "certificateOfService.pdf",
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                CERTIFICATE_OF_SERVICE_TEMPLATE_ID,
                ENGLISH,
                "certificateOfService"
            ))
            .thenReturn(certificateOfServiceDocument);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            issueBailiffPack.aboutToSubmit(details, details);

        Bailiff bailiff = aboutToStartOrSubmitResponse.getData().getAlternativeService().getBailiff();

        var cosDivorceDocument = DivorceDocument
            .builder()
            .documentLink(certificateOfServiceDocument)
            .documentFileName(certificateOfServiceDocument.getFilename())
            .documentType(CERTIFICATE_OF_SERVICE)
            .build();
        assertThat(bailiff.getCertificateOfServiceDocument()).isEqualTo(cosDivorceDocument);

        final ListValue<DivorceDocument> cosDocumentForDocumentTab = ListValue.<DivorceDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(cosDivorceDocument)
            .build();

        var generatedDoc = aboutToStartOrSubmitResponse
            .getData()
            .getDocuments()
            .getDocumentsGenerated()
            .get(FIRST);

        assertThat(generatedDoc.getValue()).isEqualTo(cosDocumentForDocumentTab.getValue());

        verify(certificateOfServiceContent).apply(caseData, TEST_CASE_ID);
        verify(caseDataDocumentService).renderDocument(
            templateContent,
            TEST_CASE_ID,
            CERTIFICATE_OF_SERVICE_TEMPLATE_ID,
            ENGLISH,
            "certificateOfService"
        );
    }
}
