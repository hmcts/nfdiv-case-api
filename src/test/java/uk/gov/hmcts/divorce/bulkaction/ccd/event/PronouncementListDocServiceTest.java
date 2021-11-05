package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.PronouncementListDocService;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.PronouncementListTemplateContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.PRONOUNCEMENT_LIST_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.PRONOUNCEMENT_LIST_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;


@ExtendWith(MockitoExtension.class)
public class PronouncementListDocServiceTest {

    @InjectMocks
    PronouncementListDocService pronouncementListDocService;

    @Mock
    private PronouncementListTemplateContent templateContentService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @Test
    void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateOrderToDispenseDocIfApplicationIsGrantedAndTypeIsDispensed() {

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkListCaseDetails = new CaseDetails<>();
        bulkListCaseDetails.setData(BulkActionCaseData.builder()
            .pronouncementJudge("Judge Bloggs")
            .build());
        bulkListCaseDetails.setId(1L);
        bulkListCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();
        when(templateContentService.apply(bulkListCaseDetails.getData(),
            bulkListCaseDetails.getId(),
            bulkListCaseDetails.getCreatedDate().toLocalDate()))
            .thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var pronouncementListDoc = new Document(
            documentUrl,
            "pronouncementList",
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                1L,
                PRONOUNCEMENT_LIST_TEMPLATE_ID,
                LanguagePreference.ENGLISH,
                PRONOUNCEMENT_LIST_DOCUMENT_NAME))
            .thenReturn(pronouncementListDoc);

        pronouncementListDocService.generateDocument(bulkListCaseDetails, bulkListCaseDetails.getData().getBulkListCaseDetails());

        assertThat(bulkListCaseDetails.getData().getPronouncementListDocument().getDocumentFileName().equals("pronouncementList"));

    }

}
