package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.task.DivorceApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicant1SolicitorAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class SolicitorUpdateApplicationApplicationServiceTest {

    @Mock
    private DivorceApplicationRemover divorceApplicationRemover;

    @Mock
    private DivorceApplicationDraft divorceApplicationDraft;

    @Mock
    private SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    @InjectMocks
    private SolicitorUpdateApplicationService solicitorUpdateApplicationService;

    @Test
    void shouldCompleteStepsToUpdateApplication() {

        final CaseData caseData = mock(CaseData.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(setApplicant1SolicitorAddress.apply(caseDetails)).thenReturn(caseDetails);
        when(divorceApplicationRemover.apply(caseDetails)).thenReturn(caseDetails);
        when(divorceApplicationDraft.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> result = solicitorUpdateApplicationService.aboutToSubmit(caseDetails);

        assertThat(result.getData(), is(caseData));
    }
}
