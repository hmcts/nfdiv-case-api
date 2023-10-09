package uk.gov.hmcts.divorce.caseworker.event;

import org.elasticsearch.core.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCorrectPaperCase.CORRECT_PAPER_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseworkerCorrectPaperCaseTest {

    @InjectMocks
    private CaseworkerCorrectPaperCase caseworkerCorrectPaperCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCorrectPaperCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CORRECT_PAPER_CASE);
    }

    @Test
    void shouldPopulateLabel() {
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

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
            .theApplicantOrApplicant1UC("Applicant 1")
            .applicantOrApplicant1UC("Applicant 1")
            .divorceOrEndingCivilPartnership("divorce")
            .finaliseDivorceOrLegallyEndYourCivilPartnership("finalise the divorce")
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCorrectPaperCase.aboutToStart(caseDetails);

        assertThat(response.getData().getLabelContent()).isEqualTo(labelContent);
    }

    @Test
    void shouldClearWarnings() {
        final CaseData caseData = new CaseData();
        caseData.getBulkScanMetaInfo().setWarnings(
            List.of(
                ListValue.<String>builder()
                    .value("Warning about HWF")
                    .build(),
                ListValue.<String>builder()
                    .value("Warning about prayer")
                    .build(),
                ListValue.<String>builder()
                    .value("Warning about something else")
                    .build()
            )
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCorrectPaperCase.aboutToSubmit(details, details);

        assertThat(response.getData().getBulkScanMetaInfo().getWarnings()).isNull();
    }
}
