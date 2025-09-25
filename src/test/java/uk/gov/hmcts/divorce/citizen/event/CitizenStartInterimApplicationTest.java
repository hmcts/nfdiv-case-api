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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.citizen.event.CitizenStartInterimApplication.CITIZEN_START_INTERIM_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CitizenStartInterimApplicationTest {

    @Mock
    DocumentRemovalService documentRemovalService;

    @InjectMocks
    private CitizenStartInterimApplication citizenStartInterimApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenStartInterimApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_START_INTERIM_APPLICATION);
    }

    @Test
    void shouldLeaveApplicationOptionsUnchangedIfApplicationTypeHasNotChanged() {
        InterimApplicationOptions options = buildInterimApplicationOptions();
        options.setInterimApplicationType(InterimApplicationType.DEEMED_SERVICE);

        final CaseDetails<CaseData, State> beforeDetails = buildCaseDetails(options);
        final CaseDetails<CaseData, State> afterDetails = buildCaseDetails(options);

        AboutToStartOrSubmitResponse<CaseData, State> response = citizenStartInterimApplication.aboutToSubmit(
            afterDetails, beforeDetails
        );

        verifyNoInteractions(documentRemovalService);

        InterimApplicationOptions responseApplicationOptions = response.getData().getApplicant1().getInterimApplicationOptions();
        assertThat(responseApplicationOptions).isEqualTo(options);
    }

    @Test
    void shouldResetInterimApplicationOptoionsAndDeleteDocumentsIfApplicationTypeChanged() {
        InterimApplicationOptions beforeOptions = buildInterimApplicationOptions();
        beforeOptions.setInterimApplicationType(InterimApplicationType.DISPENSE_WITH_SERVICE);

        InterimApplicationOptions afterOptions = buildInterimApplicationOptions();
        afterOptions.setInterimApplicationType(InterimApplicationType.DEEMED_SERVICE);

        final CaseDetails<CaseData, State> beforeDetails = buildCaseDetails(beforeOptions);
        final CaseDetails<CaseData, State> afterDetails = buildCaseDetails(afterOptions);

        AboutToStartOrSubmitResponse<CaseData, State> response = citizenStartInterimApplication.aboutToSubmit(
            afterDetails, beforeDetails
        );

        verify(documentRemovalService).deleteDocument(beforeOptions.getInterimAppsEvidenceDocs());

        InterimApplicationOptions responseApplicationOptions = response.getData().getApplicant1().getInterimApplicationOptions();
        assertThat(responseApplicationOptions.getInterimApplicationType()).isEqualTo(InterimApplicationType.DEEMED_SERVICE);
        assertThat(responseApplicationOptions.getInterimAppsUseHelpWithFees()).isNull();
        assertThat(responseApplicationOptions.getInterimAppsHaveHwfReference()).isNull();
        assertThat(responseApplicationOptions.getInterimAppsHwfRefNumber()).isNull();
        assertThat(responseApplicationOptions.getInterimAppsCanUploadEvidence()).isNull();
        assertThat(responseApplicationOptions.getInterimAppsCannotUploadDocs()).isNull();
        assertThat(responseApplicationOptions.getInterimAppsEvidenceDocs()).isNull();
    }

    @Test
    void shouldHandleBlankDocumentsList() {
        InterimApplicationOptions beforeOptions = buildInterimApplicationOptions();
        beforeOptions.setInterimApplicationType(InterimApplicationType.DISPENSE_WITH_SERVICE);

        InterimApplicationOptions afterOptions = buildInterimApplicationOptions();
        afterOptions.setInterimApplicationType(InterimApplicationType.DEEMED_SERVICE);
        afterOptions.setInterimAppsEvidenceDocs(null);

        final CaseDetails<CaseData, State> beforeDetails = buildCaseDetails(beforeOptions);
        final CaseDetails<CaseData, State> afterDetails = buildCaseDetails(afterOptions);

        citizenStartInterimApplication.aboutToSubmit(
            afterDetails, beforeDetails
        );

        verifyNoInteractions(documentRemovalService);
    }

    private InterimApplicationOptions buildInterimApplicationOptions() {
        return InterimApplicationOptions.builder()
            .interimAppsHaveHwfReference(YesOrNo.YES)
            .interimAppsHwfRefNumber("test number")
            .interimAppsCanUploadEvidence(YesOrNo.YES)
            .interimAppsCannotUploadDocs(YesOrNo.YES)
            .interimAppsEvidenceDocs(List.of(
                ListValue.<DivorceDocument>builder().value(
                    DivorceDocument.builder()
                        .documentType(DocumentType.NAME_CHANGE_EVIDENCE)
                        .build()
                ).build())
            ).noResponseJourneyOptions(
                NoResponseJourneyOptions.builder()
                    .noResponseRespondentAddressInEnglandWales(YesOrNo.YES)
                    .build()
            ).build();
    }

    private CaseDetails<CaseData, State> buildCaseDetails(InterimApplicationOptions options) {
        CaseData caseData = caseData();
        caseData.getApplicant1().setInterimApplicationOptions(options);

        return CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(AosOverdue)
            .data(caseData)
            .build();
    }
}
