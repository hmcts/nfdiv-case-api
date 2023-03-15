package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.systemupdate.event.AdminUnlinkApplicant1FromCase.ADMIN_UNLINK_APPLICANT_1;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class AdminUnlinkApplicant1FromCaseTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @InjectMocks
    private AdminUnlinkApplicant1FromCase adminUnlinkApplicant1FromCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        withEnvironmentVariable("ADMIN_UNLINK_APPLICANT_1_ENABLED", "true")
            .execute(() -> adminUnlinkApplicant1FromCase.configure(configBuilder));

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(ADMIN_UNLINK_APPLICANT_1);
    }

    @Test
    public void shouldUnlinkApplicant1() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = caseData();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        adminUnlinkApplicant1FromCase.aboutToSubmit(caseDetails, caseDetails);

        verify(ccdAccessService).removeUsersWithRole(eq(TEST_CASE_ID), eq(List.of(CREATOR.getRole())));
    }
}
