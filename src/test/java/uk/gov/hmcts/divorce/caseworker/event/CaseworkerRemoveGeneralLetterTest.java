package uk.gov.hmcts.divorce.caseworker.event;


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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        GeneralLetterDetails letterOne = getGeneralLetter(1);
        GeneralLetterDetails letterTwo = getGeneralLetter(2);
        GeneralLetterDetails letterThree = getGeneralLetterWithAttachment(3);
        GeneralLetterDetails letterFour = getGeneralLetterWithAttachment(4);

        final CaseDetails<CaseData, State> inputDetails = getCaseDetails();
        inputDetails.getData().setGeneralLetters(
            getLettersAsListValues(letterOne, letterTwo, letterThree, letterFour)
        );

        final CaseDetails<CaseData, State> outputDetails = getCaseDetails();
        outputDetails.getData().setGeneralLetters(
            getLettersAsListValues(letterOne, letterThree, letterFour)
        );

        caseworkerRemoveGeneralLetter.aboutToSubmit(outputDetails, inputDetails);

        verify(documentRemovalService).deleteDocument(letterTwo.getGeneralLetterLink());
        verifyNoMoreInteractions(documentRemovalService);
    }

    @Test
    void shouldDeleteLettersWithAttachmentsFromDocStore() {
        GeneralLetterDetails letterOne = getGeneralLetter(1);
        GeneralLetterDetails letterTwo = getGeneralLetter(2);
        GeneralLetterDetails letterThree = getGeneralLetterWithAttachment(3);
        GeneralLetterDetails letterFour = getGeneralLetterWithAttachment(4);

        final CaseDetails<CaseData, State> inputDetails = getCaseDetails();
        inputDetails.getData().setGeneralLetters(
            getLettersAsListValues(letterOne, letterTwo, letterThree, letterFour)
        );

        final CaseDetails<CaseData, State> outputDetails = getCaseDetails();
        outputDetails.getData().setGeneralLetters(
            getLettersAsListValues(letterOne, letterTwo, letterFour)
        );

        caseworkerRemoveGeneralLetter.aboutToSubmit(outputDetails, inputDetails);

        verify(documentRemovalService).deleteDocument(letterThree.getGeneralLetterLink());
        verify(documentRemovalService).deleteDocument(letterThree.getAttachments().get(0));
        verifyNoMoreInteractions(documentRemovalService);
    }

    @Test
    void shouldHandleNullAfterLetters() {
        GeneralLetterDetails letterOne = getGeneralLetter(1);
        GeneralLetterDetails letterTwo = getGeneralLetter(2);
        GeneralLetterDetails letterThree = getGeneralLetterWithAttachment(3);
        GeneralLetterDetails letterFour = getGeneralLetterWithAttachment(4);

        final CaseDetails<CaseData, State> inputDetails = getCaseDetails();
        inputDetails.getData().setGeneralLetters(
            getLettersAsListValues(letterOne, letterTwo, letterThree, letterFour)
        );

        final CaseDetails<CaseData, State> outputDetails = getCaseDetails();
        outputDetails.getData().setGeneralLetters(null);

        caseworkerRemoveGeneralLetter.aboutToSubmit(outputDetails, inputDetails);

        verify(documentRemovalService).deleteDocument(letterOne.getGeneralLetterLink());
        verify(documentRemovalService).deleteDocument(letterTwo.getGeneralLetterLink());
        verify(documentRemovalService).deleteDocument(letterThree.getGeneralLetterLink());
        verify(documentRemovalService).deleteDocument(letterThree.getAttachments().get(0));
        verify(documentRemovalService).deleteDocument(letterFour.getGeneralLetterLink());
        verify(documentRemovalService).deleteDocument(letterFour.getAttachments().get(0));
        verifyNoMoreInteractions(documentRemovalService);
    }

    private List<ListValue<GeneralLetterDetails>> getLettersAsListValues(GeneralLetterDetails... letters) {
        return Arrays.stream(letters)
            .map(letter -> ListValue.<GeneralLetterDetails>builder().value(letter).build())
            .collect(Collectors.toList());
    }

    private uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> getCaseDetails() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }

    private GeneralLetterDetails getGeneralLetter(int letterId) {
        return GeneralLetterDetails.builder()
            .generalLetterLink(Document.builder().url(String.valueOf(letterId)).build())
            .generalLetterDateTime(
                LocalDateTime.of(2020, 5, 5, 5, 5 + letterId)
            )
            .build();
    }

    private GeneralLetterDetails getGeneralLetterWithAttachment(int letterId) {
        GeneralLetterDetails letter = getGeneralLetter(letterId);
        letter.setGeneralLetterAttachmentLinks(List.of(
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
