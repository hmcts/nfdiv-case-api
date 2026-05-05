package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.CourtOrderRegeneratedTemplateContent.HAS_CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.content.CourtOrderRegeneratedTemplateContent.HAS_CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.content.CourtOrderRegeneratedTemplateContent.HAS_FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
class CourtOrderRegeneratedTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private CourtOrderRegeneratedTemplateContent courtOrderRegeneratedTemplateContent;

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .emailAddress("contactdivorce@justice.gov.uk")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

    @Test
    void shouldMapTemplateContentForDivorceApplication() {
        var caseData = TestDataHelper.caseData();
        caseData.getConditionalOrder().setCertificateOfEntitlementDocument(DivorceDocument.builder().build());
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = courtOrderRegeneratedTemplateContent
                .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(FIRST_NAME, "test_first_name"),
                entry(LAST_NAME, "test_last_name"),
                entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
                entry(HAS_CERTIFICATE_OF_ENTITLEMENT, "Yes"),
                entry(HAS_FINAL_ORDER_GRANTED, "No"),
                entry(HAS_CONDITIONAL_ORDER_GRANTED, "No"),
                entry(THE_APPLICATION, DIVORCE_APPLICATION)
        );
    }
}
