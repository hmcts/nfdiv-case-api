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
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUnlinkApplicantFromCase.SYSTEM_UNLINK_APPLICANT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemUnlinkApplicantFromCaseTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SystemUnlinkApplicantFromCase systemUnlinkApplicantFromCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemUnlinkApplicantFromCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_UNLINK_APPLICANT);
    }

    @Test
    public void shouldUnlinkApplicant() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = caseData();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        systemUnlinkApplicantFromCase.aboutToSubmit(caseDetails, caseDetails);

        verify(ccdAccessService).removeUsersWithRole(anyLong(), eq(List.of(CREATOR.getRole())));
    }
}
