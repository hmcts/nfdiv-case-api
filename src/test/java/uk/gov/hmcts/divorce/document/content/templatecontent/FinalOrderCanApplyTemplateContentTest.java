package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

class FinalOrderCanApplyTemplateContentTest {

    private FinalOrderCanApplyTemplateContent finalOrderCanApplyTemplateContent;

    @BeforeEach
    void setUp() {
        finalOrderCanApplyTemplateContent = new FinalOrderCanApplyTemplateContent();
    }

    @Test
    void testGetSupportedTemplates() {
        assertIterableEquals(finalOrderCanApplyTemplateContent.getSupportedTemplates(), List.of(FINAL_ORDER_CAN_APPLY_TEMPLATE_ID));
    }

    @Test
    void testGetTemplateContent() {
        Long caseId = new Random().nextLong();
        final AddressGlobalUK address = AddressGlobalUK.builder().addressLine1("La la land").build();
        final String firstName = "Wibble";
        final String lastName = "Wobble";
        Applicant applicant = Applicant.builder()
            .firstName(firstName)
            .lastName(lastName)
            .address(
                address
            )
            .build();

        final Map<String, Object> templateContent = finalOrderCanApplyTemplateContent.getTemplateContent(null, caseId, applicant);
        assertEquals(templateContent,
            Map.of(
                "caseReference", formatId(caseId),
                "applicantFirstName", firstName,
                "applicantLastName", lastName,
                "applicantAddress", address.getAddressLine1()
            )
        );
    }
}
