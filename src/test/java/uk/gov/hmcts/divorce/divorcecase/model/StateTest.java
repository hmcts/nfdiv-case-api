package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.lang.reflect.Field;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class StateTest {

    @Test
    void shouldBeInAlphabeticalOrderByCcdName() {

        final State[] sortedStates = stream(State.values())
            .sorted((o1, o2) -> getCcdName(o1).compareToIgnoreCase(getCcdName(o2)))
            .collect(toList())
            .toArray(new State[State.values().length]);

        assertThat(State.values()).containsExactly(sortedStates);
    }

    private String getCcdName(State state) {
        try {
            final Field field = State.class.getField(state.name());
            final CCD annotation = field.getAnnotation(CCD.class);
            if (null == annotation) {
                throw new AssertionError("CCD name not set for State");
            }
            return annotation.name();
        } catch (final NoSuchFieldException e) {
            throw new AssertionError("Field not found in State");
        }
    }
}
