package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.content.templatecontent.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAmendedApplication.CASEWORKER_ISSUE_AMENDED_APPLICATION;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AMENDED_APPLICATION_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AMENDED_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseworkerIssueAmendedApplicationTest {

    @Mock
    private Clock clock;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @InjectMocks
    private CaseworkerIssueAmendedApplication caseworkerIssueAmendedApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerIssueAmendedApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_AMENDED_APPLICATION);
    }

    @Test
    void shouldSetDueDateAndOfflineFlagsOnAndGenerateCoversheetInAboutToSubmit() {

        setMockClock(clock);

        final ListValue<DivorceDocument> amendedApplicationDocumentListValue =
            ListValue.<DivorceDocument>builder()
                .id(AMENDED_APPLICATION.getLabel())
                .value(DivorceDocument.builder()
                    .documentType(AMENDED_APPLICATION)
                    .build())
                .build();

        final Applicant applicant = Applicant.builder().languagePreferenceWelsh(NO).build();

        final CaseData caseData = CaseData.builder().build();

        caseData.getDocuments().setAmendedApplications(List.of(amendedApplicationDocumentListValue));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        var templateContent = TestDataHelper.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());
        when(coversheetApplicantTemplateContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1())).thenReturn(templateContent);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerIssueAmendedApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDueDate()).isEqualTo(LocalDate.now(clock).plusDays(16));
        assertThat(response.getData().getApplicant1().getOffline()).isEqualTo(YES);
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YES);

        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                templateContent,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, AMENDED_APPLICATION_COVERSHEET_DOCUMENT_NAME, now(clock))
            );
    }

    @Test
    void shouldReturnErrorsIfAmendedApplicationsIsEmpty() {

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerIssueAmendedApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Amended application is not uploaded");
    }
}
