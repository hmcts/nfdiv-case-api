package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDICIAL_SEPARATION_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDICIAL_SEPARATION_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.PaperApplicationReceivedTemplateContent.DATE_OF_RESPONSE;
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
    void shouldMapTemplateContentForDivorceApplication() {
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
                entry(THE_APPLICATION, DIVORCE_APPLICATION),
                entry(DATE_OF_RESPONSE, "29 March 2022")
        );
    }

    @Test
    void shouldMapWelshTemplateContentForDivorceApplication() {
        setUp();
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(WELSH));

        final Map<String, Object> templateContent = paperApplicationReceivedTemplateContent
            .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RECIPIENT_NAME, "test_first_name test_middle_name test_last_name"),
            entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry(THE_APPLICATION, DIVORCE_APPLICATION_CY),
            entry(DATE_OF_RESPONSE, "29 March 2022")
        );
    }

    @Test
    void shouldMapTemplateContentForDissolutionApplication() {
        setUp();
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = paperApplicationReceivedTemplateContent
            .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RECIPIENT_NAME, "test_first_name test_middle_name test_last_name"),
            entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry(THE_APPLICATION, END_CIVIL_PARTNERSHIP),
            entry(DATE_OF_RESPONSE, "29 March 2022")
        );
    }

    @Test
    void shouldMapWelshTemplateContentForDissolutionApplication() {
        setUp();
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(WELSH));

        final Map<String, Object> templateContent = paperApplicationReceivedTemplateContent
            .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RECIPIENT_NAME, "test_first_name test_middle_name test_last_name"),
            entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry(THE_APPLICATION, END_CIVIL_PARTNERSHIP_CY),
            entry(DATE_OF_RESPONSE, "29 March 2022")
        );
    }

    @Test
    void shouldMapTemplateContentForJudicialSeparations() {
        setUp();
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());
        caseData.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> templateContent = paperApplicationReceivedTemplateContent
            .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RECIPIENT_NAME, "test_first_name test_middle_name test_last_name"),
            entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry(THE_APPLICATION, JUDICIAL_SEPARATION_APPLICATION),
            entry(DATE_OF_RESPONSE, "29 March 2022")
        );
    }

    @Test
    void shouldMapWelshTemplateContentForJudicialSeparations() {
        setUp();
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());
        caseData.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(WELSH));

        final Map<String, Object> templateContent = paperApplicationReceivedTemplateContent
            .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RECIPIENT_NAME, "test_first_name test_middle_name test_last_name"),
            entry(RECIPIENT_ADDRESS, "line 1\ntown\npostcode"),
            entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
            entry(THE_APPLICATION, JUDICIAL_SEPARATION_APPLICATION_CY),
            entry(DATE_OF_RESPONSE, "29 March 2022")
        );
    }
}
