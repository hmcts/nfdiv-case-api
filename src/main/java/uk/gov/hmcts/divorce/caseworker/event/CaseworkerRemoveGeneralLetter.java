package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@RequiredArgsConstructor
public class CaseworkerRemoveGeneralLetter implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_REMOVE_GENERAL_LETTER = "caseworker-remove-general-letter";

    private final DocumentRemovalService documentRemovalService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_GENERAL_LETTER)
            .forStates(POST_SUBMISSION_STATES)
            .name("Remove general letter")
            .description("Remove general letter")
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, CASE_WORKER))
//            .grantHistoryOnly(CASE_WORKER))
            .page("removeGeneralLetter")
            .pageLabel("Remove general letter")
            .optional(CaseData::getGeneralLetters)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final var beforeLetters = beforeDetails.getData().getGeneralLetters();
        final var afterLetters = details.getData().getGeneralLetters();

        findRemovedLetters(beforeLetters, afterLetters)
            .forEach(this::deleteLetterAndAttachments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private Set<GeneralLetterDetails> findRemovedLetters(
        final List<ListValue<GeneralLetterDetails>> beforeLetters,
        final List<ListValue<GeneralLetterDetails>> afterLetters
    ) {
        var removedLetters = aggregateLetterDetails(beforeLetters);
        var currentLetters = aggregateLetterDetails(afterLetters);

        removedLetters.removeAll(currentLetters);

        return removedLetters;
    }

    private Set<GeneralLetterDetails> aggregateLetterDetails(List<ListValue<GeneralLetterDetails>> letters) {
        return Optional.ofNullable(letters)
            .orElse(Collections.emptyList())
            .stream()
            .map(ListValue::getValue)
            .collect(Collectors.toSet());
    }

    private void deleteLetterAndAttachments(GeneralLetterDetails letter) {
        Stream.concat(Stream.of(letter.getGeneralLetterLink()), letter.getAttachedDocuments().stream())
            .filter(Objects::nonNull)
            .forEach(documentRemovalService::deleteDocument);
    }
}
