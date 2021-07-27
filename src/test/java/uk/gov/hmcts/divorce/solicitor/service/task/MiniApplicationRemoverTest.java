package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.DraftApplicationRemovalService;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class MiniApplicationRemoverTest {

    @Mock
    private DraftApplicationRemovalService draftApplicationRemovalService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MiniApplicationRemover miniApplicationRemover;

    @Test
    void shouldRemoveDraftApplication() {

        final var generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final var caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(draftApplicationRemovalService.removeDraftApplicationDocument(generatedDocuments, TEST_CASE_ID, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(emptyList());

        final var result = miniApplicationRemover.apply(caseDetails);
        assertThat(result.getData().getDocumentsGenerated(), empty());
        verify(draftApplicationRemovalService)
            .removeDraftApplicationDocument(
                generatedDocuments,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN);

        verifyNoMoreInteractions(draftApplicationRemovalService);
    }
}
