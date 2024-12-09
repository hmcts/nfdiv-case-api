package uk.gov.hmcts.divorce.caseworker.event;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.model.CaseNote;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRemoveNote.ERROR_NOTE_ADDED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRemoveNoteTest {
    @InjectMocks
    private CaseworkerRemoveNote caseworkerRemoveNote;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRemoveNote.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CaseworkerRemoveNote.CASEWORKER_REMOVE_NOTE);
    }

    @Test
    void shouldReturnValidationErrorIfNoteIsAdded() {

        final ListValue<CaseNote> note1 = ListValue.<CaseNote>builder()
            .value(CaseNote.builder().note("Note 1").build())
            .build();
        CaseData beforeCaseData = CaseData.builder()
            .notes(List.of(note1))
            .build();
        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        final ListValue<CaseNote> note2 = ListValue.<CaseNote>builder()
            .value(CaseNote.builder().note("Note 2").build())
            .build();
        CaseData currentCaseData = CaseData.builder()
            .notes(List.of(note1, note2))
            .build();
        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        var response = caseworkerRemoveNote.midEvent(currentDetails, beforeDetails);
        assertThat(response.getErrors()).contains(ERROR_NOTE_ADDED);
    }

    @Test
    void shouldNotReturnValidationErrorIfNoteIsRemoved() {

        final ListValue<CaseNote> note1 = ListValue.<CaseNote>builder()
            .value(CaseNote.builder().note("Note 1").build())
            .build();
        CaseData beforeCaseData = CaseData.builder()
            .notes(List.of(note1))
            .build();
        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .notes(List.of())
            .build();
        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        var response = caseworkerRemoveNote.midEvent(currentDetails, beforeDetails);
        assertThat(response.getErrors()).isNullOrEmpty();
    }
}
