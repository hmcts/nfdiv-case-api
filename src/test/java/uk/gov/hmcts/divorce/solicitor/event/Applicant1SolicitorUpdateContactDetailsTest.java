package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant1SolUpdateContactDetails;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicant1SolicitorAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorUpdateContactDetails.APP1_SOLICITOR_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class Applicant1SolicitorUpdateContactDetailsTest {

    @Mock
    private Applicant1SolUpdateContactDetails solUpdateContactDetails;

    @Mock
    private SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    @InjectMocks
    private Applicant1SolicitorUpdateContactDetails applicant1SolicitorUpdateContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant1SolicitorUpdateContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APP1_SOLICITOR_UPDATE_CONTACT_DETAILS);
    }
}
