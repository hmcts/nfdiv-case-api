package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.task.MiniApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.task.MiniApplicationRemover;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class SolicitorUpdateApplicationApplicationServiceTest {

    @Mock
    private MiniApplicationRemover miniApplicationRemover;

    @Mock
    private MiniApplicationDraft miniApplicationDraft;

    @InjectMocks
    private SolicitorUpdateApplicationService solicitorUpdateApplicationService;

    @Test
    void shouldCompleteStepsToUpdateApplication() {

        final CaseData caseData = mock(CaseData.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(miniApplicationRemover.apply(caseDetails)).thenReturn(caseDetails);
        when(miniApplicationDraft.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> result = solicitorUpdateApplicationService.aboutToSubmit(caseDetails);

        assertThat(result.getData(), is(caseData));
    }
}
