package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenGenerateProcessServerDocs.CITIZEN_GENERATE_PROCESS_SERVER_DOCS;
import static uk.gov.hmcts.divorce.citizen.event.CitizenGenerateProcessServerDocs.CONFIDENTIAL_RESPONDENT_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForReIssueApplication;

@ExtendWith(MockitoExtension.class)
class CitizenGenerateProcessServerDocsTest {
    @Mock
    private GenerateApplication generateApplication;

    @Mock
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Mock
    private GenerateD10Form generateD10Form;

    @Mock
    private SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    @InjectMocks
    private CitizenGenerateProcessServerDocs generateProcessServerDocs;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        generateProcessServerDocs.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_GENERATE_PROCESS_SERVER_DOCS);
    }

    @Test
    void shouldRejectDocGenerationIfTheRespondentIsConfidential() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PRIVATE);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            generateProcessServerDocs.aboutToSubmit(caseDetails, null);


        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(CONFIDENTIAL_RESPONDENT_ERROR);
    }
}
