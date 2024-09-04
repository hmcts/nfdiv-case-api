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

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateJsCitizenAosResponseCoverLetter.APP1_ONLINE_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateJsCitizenAosResponseCoverLetter.NOT_JS_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateJsCitizenAosResponseCoverLetter.NO_RESPONSE_PACK_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateJsCitizenAosResponseCoverLetter.RESPONSE_ALREADY_SENT_ERROR;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateJsCitizenAosResponseCoverLetter.SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
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
    void shouldNotRegenerateAosResponsePackWhenAlreadyRegenerated() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setJsCitizenAosResponseLettersResent(YES);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateJsCitizenAosResponseCoverLetter.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(RESPONSE_ALREADY_SENT_ERROR));
        verifyNoInteractions(resendJSCitizenAOSResponseLetters);
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
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(NOT_JS_ERROR));
        verifyNoInteractions(resendJSCitizenAOSResponseLetters);
    }

    @Test
    void shouldNotRegenerateAosResponsePackWhenApp1IsNotOffline() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().applicant1(getApplicant()).supplementaryCaseType(JUDICIAL_SEPARATION).build();
        caseData.getApplicant1().setOffline(NO);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateJsCitizenAosResponseCoverLetter.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APP1_ONLINE_ERROR));
        verifyNoInteractions(resendJSCitizenAOSResponseLetters);
    }

    @Test
    void shouldNotRegenerateAosResponsePackWhenThereIsNoExistingAosResponseLetterDocOnJsCase() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().applicant1(getApplicant()).supplementaryCaseType(JUDICIAL_SEPARATION).build();
        caseData.getApplicant1().setOffline(YES);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateJsCitizenAosResponseCoverLetter.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(NO_RESPONSE_PACK_ERROR));
        verifyNoInteractions(resendJSCitizenAOSResponseLetters);
    }

    @Test
    void shouldRegenerateAosResponsePackWhenAosResponseLetterDocExistsOnJsCaseAndApp1IsOffline() {

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
            .applicant1(getApplicant())
            .build();
        caseData.getApplicant1().setOffline(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(resendJSCitizenAOSResponseLetters.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateJsCitizenAosResponseCoverLetter.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getErrors()).isNull();
        verify(resendJSCitizenAOSResponseLetters).apply(caseDetails);
    }
}
