package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.CivilPartnershipBroken.CIVIL_PARTNERSHIP_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageBroken.MARRIAGE_BROKEN;
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
    void shouldMigrateGeneralApplicationFeeAccountNumberToPbaNumbers() {
        String feeCode = "FEE002";
        final var data = new HashMap<String, Object>();
        data.put("generalApplicationFeeAccountNumber", feeCode);

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("generalApplicationFeePbaNumbers",
                DynamicList
                    .builder()
                    .value(DynamicListElement.builder().label(feeCode).build())
                    .listItems(List.of(DynamicListElement.builder().label(feeCode).build()))
                    .build()
            ),
            entry("generalApplicationFeeAccountNumber", null)
        );
    }

    @Test
    void shouldMigrateApplicant1ScreenHasMarriageBrokenToApplicant1HasMarriageBrokenDivorce() {
        final var data = new HashMap<String, Object>();
        data.put("applicant1ScreenHasMarriageBroken", YES);
        data.put("divorceOrDissolution", DIVORCE);

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("applicant1HasMarriageBroken", MARRIAGE_BROKEN),
            entry("applicant1ScreenHasMarriageBroken", null)
        );
    }

    @Test
    void shouldMigrateApplicant1ScreenHasMarriageBrokenToApplicant1HasMarriageBrokenDissolution() {
        final var data = new HashMap<String, Object>();
        data.put("applicant1ScreenHasMarriageBroken", YES);
        data.put("divorceOrDissolution", DISSOLUTION);

        final var result = RetiredFields.migrate(data);

        assertThat(result).contains(
            entry("applicant1HasCivilPartnershipBroken", CIVIL_PARTNERSHIP_BROKEN),
            entry("applicant1ScreenHasMarriageBroken", null)
        );
    }
}
