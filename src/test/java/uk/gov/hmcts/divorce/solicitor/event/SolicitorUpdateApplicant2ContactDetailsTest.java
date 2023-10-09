package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.ProcessConfidentialDocumentsService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.UpdateApplicant2ContactDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorUpdateApplicant2ContactDetails.APPLICANT_UPDATE_APPLICANT2_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SolicitorUpdateApplicant2ContactDetailsTest {

    @Mock
    private UpdateApplicant2ContactDetails applicant2UpdateContactDetails;

    @Mock
    private ProcessConfidentialDocumentsService confidentialDocumentsService;

    @InjectMocks
    private SolicitorUpdateApplicant2ContactDetails solicitorUpdateApplicant2ContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorUpdateApplicant2ContactDetails.configure(configBuilder);

        verify(applicant2UpdateContactDetails).addTo(any(PageBuilder.class));

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_UPDATE_APPLICANT2_CONTACT_DETAILS);
    }

    @Test
    public void aboutToSubmitShouldCallProcessConfidentialDocuments() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().build())
            .build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        solicitorUpdateApplicant2ContactDetails.aboutToSubmit(details, null);

        verify(confidentialDocumentsService).processDocuments(caseData, details.getId());
    }
}
