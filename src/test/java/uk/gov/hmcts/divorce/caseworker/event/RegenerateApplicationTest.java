package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.common.event.RegenerateApplication;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.RegenerateApplication.REGENERATE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithMarriageDate;

@ExtendWith(MockitoExtension.class)
class RegenerateApplicationTest {

    @Mock
    private GenerateApplication generateApplication;

    @InjectMocks
    private RegenerateApplication regenerateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        regenerateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(REGENERATE_APPLICATION);
    }

    @Test
    void shouldGenerateApplication() {
        final CaseData caseData = caseDataWithMarriageDate();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(generateApplication.apply(caseDetails)).thenReturn(caseDetails);

        regenerateApplication.aboutToSubmit(caseDetails, caseDetails);

        verify(generateApplication).apply(caseDetails);
    }
}
