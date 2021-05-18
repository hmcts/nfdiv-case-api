package uk.gov.hmcts.divorce.solicitor.service.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.model.Court.SERVICE_CENTRE;

@ExtendWith(MockitoExtension.class)
class SolicitorCourtDetailsTest {

    @Mock
    private CaseDataContext caseDataContext;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private SolicitorCourtDetails solicitorCourtDetails;

    @Test
    void shouldSetSolicitorCourtDetailsInGivenCaseData() {

        final var caseData = CaseData.builder().build();

        when(caseDataContext.copyOfCaseData()).thenReturn(caseData);
        when(caseDataContext.handlerContextWith(caseData)).thenReturn(caseDataContext);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final var result = solicitorCourtDetails.updateCaseData(this.caseDataContext, caseDataUpdaterChain);

        assertThat(result, is(caseDataContext));

        assertThat(caseData.getDivorceUnit(), is(SERVICE_CENTRE));
        assertThat(caseData.getSelectedDivorceCentreSiteId(), is(SERVICE_CENTRE.getSiteId()));
    }
}
