package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.caseworker.event.page.UpdateContactDetails;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.ProcessConfidentialDocumentsService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateContactDetails.CASEWORKER_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateContactDetailsTest {

    @Mock
    private UpdateContactDetails updateContactDetails;

    @Mock
    private ProcessConfidentialDocumentsService processConfidentialDocumentsService;

    @InjectMocks
    private CaseworkerUpdateContactDetails caseworkerUpdateContactDetails;

    @Test
    public void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateContactDetails.configure(configBuilder);

        verify(updateContactDetails).addTo(any(PageBuilder.class));

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_CONTACT_DETAILS);
    }

    @Test
    public void aboutToSubmitShouldCallProcessConfidentialDocuments() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        caseworkerUpdateContactDetails.aboutToSubmit(details, null);

        verify(processConfidentialDocumentsService).processDocuments(caseData, details.getId());
    }
}
