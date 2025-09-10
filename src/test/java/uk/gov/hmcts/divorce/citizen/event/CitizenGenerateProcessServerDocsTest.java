package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenGenerateProcessServerDocs.CITIZEN_GENERATE_PROCESS_SERVER_DOCS;
import static uk.gov.hmcts.divorce.citizen.event.CitizenGenerateProcessServerDocs.CONFIDENTIAL_RESPONDENT_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.SERVICE_DOCUMENTS_ALREADY_REGENERATED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForReIssueApplication;

@ExtendWith(MockitoExtension.class)
class CitizenGenerateProcessServerDocsTest {
    @InjectMocks
    private CitizenGenerateProcessServerDocs generateProcessServerDocs;

    private static final int REISSUE_OFFSET_DAYS = 14;

    @BeforeEach
    void setPageSize() {
        ReflectionTestUtils.setField(
            generateProcessServerDocs,
            "docsRegeneratedOffsetDays",
            REISSUE_OFFSET_DAYS
        );
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        generateProcessServerDocs.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_GENERATE_PROCESS_SERVER_DOCS);
    }

    @Test
    void shouldRejectTheUpdateIfServiceDateValidationFails() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateServiceDate(caseData, REISSUE_OFFSET_DAYS))
                .thenReturn(List.of(SERVICE_DOCUMENTS_ALREADY_REGENERATED));

            final AboutToStartOrSubmitResponse<CaseData, State> response =
                generateProcessServerDocs.aboutToStart(caseDetails);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).contains(SERVICE_DOCUMENTS_ALREADY_REGENERATED);
        }
    }

    @Test
    void shouldRejectTheUpdateIfRespondentIsConfidential() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PRIVATE);
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateServiceDate(caseData, REISSUE_OFFSET_DAYS))
                .thenReturn(Collections.emptyList());

            final AboutToStartOrSubmitResponse<CaseData, State> response =
                generateProcessServerDocs.aboutToStart(caseDetails);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).contains(CONFIDENTIAL_RESPONDENT_ERROR);
        }
    }

    @Test
    void shouldAllowTheUpdateIfServiceDateValidationPassesAndRespondentNotConfidential() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PUBLIC);
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateServiceDate(caseData, REISSUE_OFFSET_DAYS))
                .thenReturn(Collections.emptyList());

            final AboutToStartOrSubmitResponse<CaseData, State> response =
                generateProcessServerDocs.aboutToStart(caseDetails);

            assertThat(response.getErrors()).isNull();
        }
    }
}
