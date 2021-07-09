package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.solicitor.service.DraftApplicationRemovalService;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
class MiniApplicationRemoverTest {

    @Mock
    private DraftApplicationRemovalService draftApplicationRemovalService;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private MiniApplicationRemover miniApplicationRemover;

    @Test
    void shouldRemoveDraftApplication() {

        final var generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final var caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);

        final var caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);
        when(draftApplicationRemovalService.removeDraftApplicationDocument(generatedDocuments, TEST_CASE_ID, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(emptyList());

        final var result = miniApplicationRemover.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(result.getCaseData().getDocumentsGenerated(), empty());
        verify(draftApplicationRemovalService)
            .removeDraftApplicationDocument(
                generatedDocuments,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN);

        verifyNoMoreInteractions(draftApplicationRemovalService, caseDataUpdaterChain);
    }
}
