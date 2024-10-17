package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CaseMatchTest {

    @Test
    void shouldBeEqualWhenCaseLinkReferencesAreTheSame() {
        CaseMatch caseMatch1 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(CaseLink.builder().caseReference("12345").build())
            .build();

        CaseMatch caseMatch2 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(CaseLink.builder().caseReference("12345").build())
            .build();

        assertThat(caseMatch1).isEqualTo(caseMatch2);
        assertThat(caseMatch1.hashCode()).isEqualTo(caseMatch2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenCaseLinkReferencesAreDifferent() {
        CaseMatch caseMatch1 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(CaseLink.builder().caseReference("12345").build())
            .build();

        CaseMatch caseMatch2 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(CaseLink.builder().caseReference("67890").build())
            .build();

        assertThat(caseMatch1).isNotEqualTo(caseMatch2);
        assertThat(caseMatch1.hashCode()).isNotEqualTo(caseMatch2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenOneCaseLinkIsNull() {
        CaseMatch caseMatch1 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(CaseLink.builder().caseReference("12345").build())
            .build();

        CaseMatch caseMatch2 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(null)
            .build();

        assertThat(caseMatch1).isNotEqualTo(caseMatch2);
        assertThat(caseMatch2).isNotEqualTo(caseMatch1);
    }

    @Test
    void shouldBeEqualWhenBothCaseLinksAreNull() {
        CaseMatch caseMatch1 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(null)
            .build();

        CaseMatch caseMatch2 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(null)
            .build();

        assertThat(caseMatch1).isEqualTo(caseMatch2);
        assertThat(caseMatch1.hashCode()).isEqualTo(caseMatch2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenComparingWithDifferentObjectType() {
        CaseMatch caseMatch1 = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(CaseLink.builder().caseReference("12345").build())
            .build();

        String differentTypeObject = "A different type object";

        assertThat(caseMatch1).isNotEqualTo(differentTypeObject);
    }

    @Test
    void shouldBeEqualToItself() {
        // Given a CaseMatch instance
        CaseMatch caseMatch = CaseMatch.builder()
            .applicant1Name("John Doe")
            .applicant2Name("Jane Doe")
            .date(LocalDate.of(2000, 1, 1))
            .caseLink(CaseLink.builder().caseReference("12345").build())
            .build();

        assertThat(caseMatch).isEqualTo(caseMatch);
    }
}

