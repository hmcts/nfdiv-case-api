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
import uk.gov.hmcts.divorce.common.service.task.SendRegeneratedCOPronouncedCoverLetters;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoverLetter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRegenerateCoPronouncedCoverLetterOfflineConfidential.SYSTEM_REGEN_CO_PRONOUNCED_COVER_LETTER_OFFLINE_CONFIDENTIAL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class SystemRegenerateCoPronouncedCoverLetterOfflineConfidentialTest {

    @Mock
    private RegenerateConditionalOrderPronouncedCoverLetter regenerateConditionalOrderPronouncedCoverLetter;

    @Mock
    private SendRegeneratedCOPronouncedCoverLetters sendRegeneratedCOPronouncedCoverLetters;

    @InjectMocks
    private SystemRegenerateCoPronouncedCoverLetterOfflineConfidential systemRegenerateCoPronouncedCoverLetterOfflineConfidential;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemRegenerateCoPronouncedCoverLetterOfflineConfidential.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REGEN_CO_PRONOUNCED_COVER_LETTER_OFFLINE_CONFIDENTIAL);
    }

    @Test
    void shouldNotRegenerateCourtOrdersForDigitalCaseWhenThereIsNoExistingCOGrantedDocument() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateCoPronouncedCoverLetterOfflineConfidential.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        verifyNoInteractions(regenerateConditionalOrderPronouncedCoverLetter);
        verifyNoInteractions(sendRegeneratedCOPronouncedCoverLetters);
    }

    @Test
    void shouldRegenerateCOGrantedDocumentWhenCOGrantedDocExists() {

        final var generatedDocuments = Lists.newArrayList(getDivorceDocumentListValue(
            "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
            "co_granted.pdf",
            CONDITIONAL_ORDER_GRANTED
        ));

        final CaseData caseData = CaseData
            .builder()
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(generatedDocuments)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(regenerateConditionalOrderPronouncedCoverLetter.apply(caseDetails)).thenReturn(caseDetails);
        when(sendRegeneratedCOPronouncedCoverLetters.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemRegenerateCoPronouncedCoverLetterOfflineConfidential.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(regenerateConditionalOrderPronouncedCoverLetter).apply(caseDetails);
        verify(sendRegeneratedCOPronouncedCoverLetters).apply(caseDetails);
    }
}
