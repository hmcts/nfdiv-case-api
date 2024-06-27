package uk.gov.hmcts.divorce.common.event;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.documentpack.SwitchToSoleCODocumentPack;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCoSendLetters.SWITCH_TO_SOLE_CO_SEND_LETTERS;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class SwitchedToSoleCoSendLettersTest {

    private static final DocumentPackInfo TEST_DOCUMENT_PACK_INFO = new DocumentPackInfo(
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER, Optional.of(SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            SWITCH_TO_SOLE_CO_LETTER_TEMPLATE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME
        )
    );
    public static final String THE_LETTER_ID = "the-letter-id";

    @Mock
    private LetterPrinter printer;

    @Mock
    private SwitchToSoleCODocumentPack switchToSoleConditionalOrderDocumentPack;

    @InjectMocks
    private SwitchedToSoleCoSendLetters switchedToSoleCoSendLetters;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        switchedToSoleCoSendLetters.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SWITCH_TO_SOLE_CO_SEND_LETTERS);
    }

    @Test
    void shouldSendLetterToRespondent() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        when(switchToSoleConditionalOrderDocumentPack.getDocumentPack(caseData, null)).thenReturn(TEST_DOCUMENT_PACK_INFO);
        when(switchToSoleConditionalOrderDocumentPack.getLetterId()).thenReturn(THE_LETTER_ID);

        switchedToSoleCoSendLetters.aboutToSubmit(caseDetails, caseDetails);

        verify(printer).sendLetters(caseData, caseId, caseData.getApplicant2(), TEST_DOCUMENT_PACK_INFO, THE_LETTER_ID);
    }
}
