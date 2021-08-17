package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class AddMiniApplicationLinkTest {

    @InjectMocks
    private AddMiniApplicationLink addMiniApplicationLink;

    @Test
    void shouldSetMiniApplicationLinkIfDivorceApplicationDocumentPresent() {

        final Document documentLink = new Document("url", "filename", "binary url");
        final ListValue<DivorceDocument> miniApplicationListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(DIVORCE_APPLICATION)
                .documentLink(documentLink)
                .build())
            .build();

        final CaseData caseData = caseData();
        caseData.setDocumentsGenerated(singletonList(miniApplicationListValue));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = addMiniApplicationLink.apply(caseDetails);

        assertThat(result.getData().getApplication().getMiniApplicationLink()).isSameAs(documentLink);
    }

    @Test
    void shouldNotSetMiniApplicationLinkIfNoDivorceApplicationDocumentPresent() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = addMiniApplicationLink.apply(caseDetails);

        assertThat(result.getData().getApplication().getMiniApplicationLink()).isNull();
    }
}