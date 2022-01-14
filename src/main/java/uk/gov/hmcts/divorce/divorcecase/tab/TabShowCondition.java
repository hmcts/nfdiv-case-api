package uk.gov.hmcts.divorce.divorcecase.tab;

import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public final class TabShowCondition {

    private TabShowCondition() {
    }

    public static String notShowForState(final State... states) {
        return Stream.of(states)
            .map(State::getName)
            .collect(joining("\" AND [STATE]!=\"", "[STATE]!=\"", "\""));
    }

    public static String showForState(final State... states) {
        return Stream.of(states)
            .map(State:: getName)
            .collect(joining("\" OR [STATE]=\"", "[STATE]=\"", "\""));
    }
}
