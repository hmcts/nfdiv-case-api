package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreateApplication.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateApplicationTest {

    @Mock
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Mock
    private SolAboutTheSolicitor solAboutTheSolicitor;

    @InjectMocks
    private SolicitorCreateApplication solicitorCreateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorCreateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_CREATE);
    }

    @Test
    public void shouldPopulateMissingRequirementsFieldsInCaseData() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        when(solicitorCreateApplicationService.aboutToSubmit(details)).thenReturn(details);

        solicitorCreateApplication.aboutToSubmit(details, beforeDetails);

        verify(solicitorCreateApplicationService).aboutToSubmit(details);
    }
}
