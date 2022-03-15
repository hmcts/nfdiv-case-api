package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCreatePaperCase.CREATE_PAPER_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerCreatePaperCaseTest {
    @InjectMocks
    private CaseworkerCreatePaperCase caseworkerCreatePaperCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        caseworkerCreatePaperCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CREATE_PAPER_CASE);
    }

    @Test
    public void shouldSetHyphenatedCaseRefAndApplicant1Offline() throws Exception {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerCreatePaperCase.aboutToSubmit(details, details);

        assertThat(submitResponse.getData().getHyphenatedCaseRef()).isEqualTo("0000-0000-0000-0001");
        assertThat(submitResponse.getData().getApplicant1().getOffline()).isEqualTo(YES);

        final var labelContent = LabelContent
            .builder()
            .applicant2("applicant 2")
            .theApplicant2("applicant 2")
            .theApplicant2UC("Applicant 2")
            .applicant2UC("Applicant 2")
            .unionType("divorce")
            .unionTypeUC("Divorce")
            .divorceOrCivilPartnershipApplication("divorce application")
            .divorceOrEndCivilPartnership("for divorce")
            .applicantOrApplicant1("applicant 1’s")
            .divorceOrCivilPartnership("divorce")
            .finaliseDivorceOrEndCivilPartnership("finalise the divorce")
            .marriageOrCivilPartnershipUC("Marriage")
            .marriageOrCivilPartnership("marriage")
            .divorceOrLegallyEnd("get a divorce")
            .applicantsOrApplicant1s("Applicant 1’s")
            .theApplicantOrApplicant1("applicant 1")
            .gotMarriedOrFormedCivilPartnership("got married")
            .respondentsOrApplicant2s("Applicant 2's")
            .applicantOrApplicant1UC("Applicant 1")
            .theApplicantOrApplicant1UC("Applicant 1")
            .build();

        assertThat(submitResponse.getData().getLabelContent()).isEqualTo(labelContent);
    }
}
