package uk.gov.hmcts.divorce.systemupdate.event;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.service.task.ResendJSCitizenAOSResponseLetters;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateJsCitizenAosResponseCoverLetter.SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class SystemRegenerateJsCitizenAosResponseCoverLetterTest {

    @Mock
    private ResendJSCitizenAOSResponseLetters resendJSCitizenAOSResponseLetters;

    @InjectMocks
    private SystemRegenerateJsCitizenAosResponseCoverLetter systemRegenerateJsCitizenAosResponseCoverLetter;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemRegenerateJsCitizenAosResponseCoverLetter.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER);
    }

    @Test
    void shouldNotRegenerateAosResponsePackWhenNotJsCase() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
                systemRegenerateJsCitizenAosResponseCoverLetter.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        verifyNoInteractions(resendJSCitizenAOSResponseLetters);
    }

    @Test
    void shouldNotRegenerateAosResponsePackWhenThereIsNoExistingAosResponseLetterDocOnJsCase() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().supplementaryCaseType(JUDICIAL_SEPARATION).build();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateJsCitizenAosResponseCoverLetter.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        verifyNoInteractions(resendJSCitizenAOSResponseLetters);
    }

    @Test
    void shouldRegenerateAosResponsePackWhenAosResponseLetterDocExistsOnJsCase() {

        final var generatedDocuments = Lists.newArrayList(getDivorceDocumentListValue(
            "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
            "aosResponseLetter.pdf",
            AOS_RESPONSE_LETTER
        ));

        final CaseData caseData = CaseData
            .builder()
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(generatedDocuments)
                    .build()
            )
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(resendJSCitizenAOSResponseLetters.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateJsCitizenAosResponseCoverLetter.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(resendJSCitizenAOSResponseLetters).apply(caseDetails);
    }
}
