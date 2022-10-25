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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAmendedApplication.CASEWORKER_ISSUE_AMENDED_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AMENDED_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CaseworkerIssueAmendedApplicationTest {

    @Mock
    private Clock clock;

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
    void shouldSetDueDateAndOfflineFlagsOnAboutToSubmit() {

        setMockClock(clock);

        final ListValue<DivorceDocument> amendedApplicationDocumentListValue =
            ListValue.<DivorceDocument>builder()
                .id(AMENDED_APPLICATION.getLabel())
                .value(DivorceDocument.builder()
                    .documentType(AMENDED_APPLICATION)
                    .build())
                .build();
        final CaseData caseData = CaseData.builder().build();
        caseData.getDocuments().setAmendedApplications(List.of(amendedApplicationDocumentListValue));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerIssueAmendedApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getDueDate()).isEqualTo(LocalDate.now(clock).plusDays(16));
        assertThat(response.getData().getApplicant1().getOffline()).isEqualTo(YES);
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YES);
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
