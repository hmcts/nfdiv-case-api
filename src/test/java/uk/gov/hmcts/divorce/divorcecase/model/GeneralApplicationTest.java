package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralApplicationTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Test
    void shouldReturnFormattedLabelShowingApplicationReceivedDateAndApplicationType() {
        assertThat(GeneralApplication.builder()
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .generalApplicationReceivedDate(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
            .build()
            .getLabel(0, formatter))
            .isEqualTo(
                "General applications 1, %s, 01 Jan 2020", GeneralApplicationType.DEEMED_SERVICE.getLabel()
            );
    }

    @Test
    void shouldHandleNullApplicationTypeWhenFormattingLabel() {
        assertThat(GeneralApplication.builder()
            .generalApplicationType(null)
            .generalApplicationReceivedDate(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
            .build()
            .getLabel(0, formatter))
            .isEqualTo("General applications 1, , 01 Jan 2020");
    }

    @Test
    void shouldHandleNullApplicationReceivedDateWhenFormattingLabel() {
        assertThat(GeneralApplication.builder()
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .generalApplicationReceivedDate(null)
            .build()
            .getLabel(0, formatter))
            .isEqualTo(
                "General applications 1, %s, ", GeneralApplicationType.DEEMED_SERVICE.getLabel()
            );
    }
}
