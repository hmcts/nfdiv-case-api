package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.PaperApplicationReceivedTemplateContent.DATE_OF_RESPONSE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
class PaperApplicationReceivedTemplateContentTest {

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private PaperApplicationReceivedTemplateContent paperApplicationReceivedTemplateContent;

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

    void setUp() {
        setMockClock(clock, LocalDate.of(2022, 3, 01));
    }

    @Test
    public void shouldMapTemplateContentWhenRecipientIsApplicant1() {
        setUp();
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = paperApplicationReceivedTemplateContent
                .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(RECIPIENT_NAME, "test_first_name test_middle_name test_last_name"),
                entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
                entry(IS_DIVORCE, true),
                entry(DATE_OF_RESPONSE, "29 March 2022")
        );
    }
}
