package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.Court.SERVICE_CENTRE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class SolicitorCourtDetailsTest {

    @InjectMocks
    private SolicitorCourtDetails solicitorCourtDetails;

    @Test
    void shouldSetSolicitorCourtDetailsInGivenCaseData() {

        final var caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var result = solicitorCourtDetails.apply(caseDetails);

        assertThat(result.getData().getDivorceUnit(), is(SERVICE_CENTRE));
        assertThat(result.getData().getSelectedDivorceCentreSiteId(), is(SERVICE_CENTRE.getSiteId()));
    }
}
