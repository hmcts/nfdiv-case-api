package uk.gov.hmcts.divorce.caseworker.event;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRemoveGeneralLetter.CASEWORKER_REMOVE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRemoveGeneralLetterTest {

    @InjectMocks
    private CaseworkerRemoveGeneralLetter caseworkerRemoveGeneralLetter;

    @Mock
    private DocumentRemovalService documentRemovalService;

    private CaseDetails<CaseData, State> beforeDetails;

    private CaseDetails<CaseData, State> afterDetails;

    @BeforeEach
    public void setUp() {
        beforeDetails = getCaseDetails();
        afterDetails = getCaseDetails();
        setUpLetters();
    }

    private void setUpLetters() {
        List<ListValue<GeneralLetterDetails>> beforeLetters = List.of(
            getGeneralLetter(1),
            getGeneralLetter(2),
            getGeneralLetterWithAttachment(3),
            getGeneralLetterWithAttachment(4)
        );

        beforeDetails.getData().setGeneralLetters(beforeLetters);
        afterDetails.getData().setGeneralLetters(new ArrayList<>(beforeLetters));
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRemoveGeneralLetter.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REMOVE_GENERAL_LETTER);
    }

    @Test
    void shouldDeleteLettersWithoutAttachmentsFromDocStore() {
        afterDetails.getData().getGeneralLetters().remove(1);

        caseworkerRemoveGeneralLetter.aboutToSubmit(afterDetails, beforeDetails);

        verifyLetterDeletion(1);
        verifyNoMoreInteractions(documentRemovalService);
    }

    @Test
    void shouldDeleteLettersWithAttachmentsFromDocStore() {
        afterDetails.getData().getGeneralLetters().remove(2);

        caseworkerRemoveGeneralLetter.aboutToSubmit(afterDetails, beforeDetails);

        verifyLetterDeletion(2);
        verifyAttachmentDeletion(2);
        verifyNoMoreInteractions(documentRemovalService);
    }

    @Test
    void shouldHandleNullAfterLetters() {
        afterDetails.getData().setGeneralLetters(null);

        caseworkerRemoveGeneralLetter.aboutToSubmit(afterDetails, beforeDetails);

        verifyLetterDeletion(0);
        verifyLetterDeletion(1);
        verifyLetterDeletion(2);
        verifyLetterDeletion(3);
        verifyAttachmentDeletion(2);
        verifyAttachmentDeletion(3);
        verifyNoMoreInteractions(documentRemovalService);
    }

    private void verifyLetterDeletion(int letterIdx) {
        List<ListValue<GeneralLetterDetails>> letters = beforeDetails.getData().getGeneralLetters();

        verify(documentRemovalService).deleteDocument(letters.get(letterIdx).getValue().getGeneralLetterLink());
    }

    private void verifyAttachmentDeletion(int letterIdx) {
        List<ListValue<GeneralLetterDetails>> letters = beforeDetails.getData().getGeneralLetters();

        verify(documentRemovalService).deleteDocument(
            letters.get(letterIdx).getValue().getGeneralLetterAttachmentLinks().get(0).getValue()
        );
    }

    private uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> getCaseDetails() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }

    private ListValue<GeneralLetterDetails> getGeneralLetter(int letterId) {
        return ListValue.<GeneralLetterDetails>builder()
                .value(
                    GeneralLetterDetails.builder()
                        .generalLetterLink(Document.builder().url(String.valueOf(letterId)).build())
                        .generalLetterDateTime(
                            LocalDateTime.of(2020, 5, 5, 5, 5 + letterId)
                        )
                        .build()
                ).build();
    }

    private ListValue<GeneralLetterDetails> getGeneralLetterWithAttachment(int letterId) {
        ListValue<GeneralLetterDetails> letter = getGeneralLetter(letterId);
        letter.getValue().setGeneralLetterAttachmentLinks(List.of(
            ListValue.<Document>builder()
                .value(
                    Document.builder()
                        .url(String.format("attachment %s", letterId))
                        .build()
                ).build()
        ));

        return letter;
    }
}
