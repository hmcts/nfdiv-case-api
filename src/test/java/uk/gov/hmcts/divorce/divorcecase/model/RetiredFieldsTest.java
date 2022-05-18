package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.HashMap;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;

class RetiredFieldsTest {

    @Test
    void migrateShouldMigrateSomeFieldsAndLeaveOthersAlone() {
        final var data = new HashMap<String, Object>();
        data.put("exampleRetiredField", "This will be first name");
        data.put("applicant1FirstName", "This will be overwritten");
        data.put("applicant1LastName", "This will be left alone");

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("applicant1FirstName", "This will be first name"),
            entry("applicant1LastName", "This will be left alone"),
            entry("exampleRetiredField", null)
        );
    }

    @Test
    void shouldMigrateSolServiceMethodToServiceMethod() {
        final var data = new HashMap<String, Object>();
        data.put("solServiceMethod", PERSONAL_SERVICE);

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("serviceMethod", PERSONAL_SERVICE),
            entry("solServiceMethod", null)
        );
    }

    @Test
    void shouldMigrateD11DocumentToAnswerReceivedDocuments() {
        DivorceDocument d11Document = DivorceDocument.builder().build();
        final var data = new HashMap<String, Object>();
        data.put("d11Document", d11Document);

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("answerReceivedSupportingDocuments", List.of(ListValue
                .<DivorceDocument>builder()
                .id("1")
                .value(d11Document)
                .build()
            )),
            entry("d11Document", null)
        );
    }

    @Test
    void shouldMigrateSOTWhenEnvFlagIsTrueAndDataIsAppropriate() throws Exception {
        final var data = new HashMap<String, Object>();
        data.put("applicationType", "soleApplication");
        data.put("applicant2StatementOfTruth", "Yes");

        final var result = withEnvironmentVariable("SOT_MIGRATION_ENABLED", "true")
            .execute(() -> RetiredFields.migrate(data));

        assertThat(result).contains(
            entry("applicationType", "soleApplication"),
            entry("applicant2StatementOfTruth", null),
            entry("statementOfTruth", "Yes")
        );
    }

    @Test
    void shouldNotMigrateSOTWhenEnvFlagIsFalse() throws Exception {
        final var data = new HashMap<String, Object>();
        data.put("applicationType", "soleApplication");
        data.put("applicant2StatementOfTruth", "Yes");

        final var result = withEnvironmentVariable("SOT_MIGRATION_ENABLED", "false")
            .execute(() -> RetiredFields.migrate(data));

        assertThat(result).contains(
            entry("applicationType", "soleApplication"),
            entry("applicant2StatementOfTruth", "Yes")
        );

        assertThat(result).doesNotContain(
            entry("statementOfTruth", "Yes")
        );
    }

    @Test
    void shouldNotMigrateSOTWhenEnvFlagIsTrueAndApplicationIsJoint() throws Exception {
        final var data = new HashMap<String, Object>();
        data.put("applicationType", "jointApplication");
        data.put("applicant2StatementOfTruth", "Yes");

        final var result = withEnvironmentVariable("SOT_MIGRATION_ENABLED", "true")
            .execute(() -> RetiredFields.migrate(data));

        assertThat(result).contains(
            entry("applicationType", "jointApplication"),
            entry("applicant2StatementOfTruth", "Yes")
        );

        assertThat(result).doesNotContain(
            entry("statementOfTruth", "Yes")
        );
    }

    @Test
    void shouldNotMigrateSOTWhenEnvFlagIsTrueAndApplicant2StatementOfTruthNotSet() throws Exception {
        final var data = new HashMap<String, Object>();
        data.put("applicationType", "soleApplication");

        final var result = withEnvironmentVariable("SOT_MIGRATION_ENABLED", "true")
            .execute(() -> RetiredFields.migrate(data));

        assertThat(result).contains(
            entry("applicationType", "soleApplication")
        );

        assertThat(result).doesNotContain(
            entry("statementOfTruth", "Yes")
        );
    }
}
