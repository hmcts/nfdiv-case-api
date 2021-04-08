package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataContext;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdaterChain;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.Court.SERVICE_CENTRE;

@ExtendWith(MockitoExtension.class)
class SolicitorCourtDetailsTest {

    private static final Instant INSTANT = Instant.parse("2021-03-31T16:00:00.00Z");
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Mock
    private CaseDataContext caseDataContext;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @Mock
    private Clock clock;

    @InjectMocks
    private SolicitorCourtDetails solicitorCourtDetails;

    @BeforeEach
    void setupClock() {
        when(clock.instant()).thenReturn(INSTANT);
        when(clock.getZone()).thenReturn(ZONE_ID);
    }

    @Test
    void shouldSetSolictorCourtDetailsInGivenCaseData() {

        final CaseData caseData = CaseData.builder().build();
        final LocalDate expected = LocalDate.ofInstant(INSTANT, ZONE_ID);

        when(caseDataContext.copyOfCaseData()).thenReturn(caseData);
        when(caseDataContext.handlerContextWith(caseData)).thenReturn(caseDataContext);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseDataContext result = solicitorCourtDetails.updateCaseData(this.caseDataContext, caseDataUpdaterChain);

        assertThat(result, is(caseDataContext));

        assertThat(caseData.getCreatedDate(), is(expected));
        assertThat(caseData.getDivorceUnit(), is(SERVICE_CENTRE));
        assertThat(caseData.getSelectedDivorceCentreSiteId(), is(SERVICE_CENTRE.getSiteId()));
    }
}