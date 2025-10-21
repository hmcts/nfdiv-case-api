package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsWhichDepartment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SEARCH_GOV_RECORDS_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
class SearchGovRecordsApplicationTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private SearchGovRecordsApplicationTemplateContent templateContent;

    @Test
    void shouldReturnTemplateContentForEnglish() {
        final CaseData caseData = caseData();
        caseData.getGeneralApplication().setGeneralApplicationReceivedDate(LocalDateTime.of(2025, Month.AUGUST, 1, 0, 0));
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        buildSearchGovRecords(caseData, true);

        final Map<String, Object> basicDocmosisTemplateContent = getBasicDocmosisTemplateContent(ENGLISH);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(basicDocmosisTemplateContent);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getGeneralApplication()
        );

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", caseData.getApplicant2().getFullName());
        expectedEntries.put("applicationDate", "1 August 2025");
        expectedEntries.put("whySearchGovRecords", "Test reason");
        expectedEntries.put("departmentsToSearch", "[Department for Work and Pensions, HM Revenue & Customs]");
        expectedEntries.put("otherDepartmentsToSearch", true);
        expectedEntries.put("otherDepartments", "Test department");
        expectedEntries.put("reasonWhySearchTheseDepartments", "Test reason");
        expectedEntries.put("partnerName", "Partner name");
        expectedEntries.put("knowPartnerDateOfBirth", "Yes");
        expectedEntries.put("partnerDateOfBirth", "1 January 1990");
        expectedEntries.put("knowPartnerNationalInsurance", "Yes");
        expectedEntries.put("partnerNationalInsurance", "12345678");
        expectedEntries.put("partnerLastKnownAddress", "address1\nUK");
        expectedEntries.put("datesPartnerLivedAtLastKnownAddress", "Test date1");
        expectedEntries.put("knowAdditionalAddressesForPartner", "Yes");
        expectedEntries.put("additionalAddress1", "Test address1");
        expectedEntries.put("additionalAddress1DatesLivedThere", "1 Jan 2023");
        expectedEntries.put("additionalAddress2", "Test address2");
        expectedEntries.put("additionalAddress2DatesLivedThere", "1 Jan 2024");
        expectedEntries.put("divorceAndDissolutionHeader","Divorce and Dissolution");
        expectedEntries.put("phoneAndOpeningTimes","0300 303 0642 (Monday to Friday, 10am to 6pm)");
        expectedEntries.put("courtsAndTribunalsServiceHeader","HM Courts & Tribunals Service");
        expectedEntries.put("contactEmail","contactdivorce@justice.gov.uk");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldReturnTemplateContentForWelsh() {
        final CaseData caseData = caseData();
        caseData.getGeneralApplication().setGeneralApplicationReceivedDate(LocalDateTime.of(2025, Month.AUGUST, 1, 0, 0));
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder().build());
        buildSearchGovRecords(caseData, false);

        final Map<String, Object> basicDocmosisTemplateContent = getBasicDocmosisTemplateContent(ENGLISH);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(basicDocmosisTemplateContent);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getGeneralApplication()
        );

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", caseData.getApplicant2().getFullName());
        expectedEntries.put("applicationDate", "1 Awst 2025");
        expectedEntries.put("whySearchGovRecords", "Test reason");
        expectedEntries.put("departmentsToSearch", "[Department for Work and Pensions, HM Revenue & Customs]");
        expectedEntries.put("otherDepartmentsToSearch", true);
        expectedEntries.put("otherDepartments", "Test department");
        expectedEntries.put("reasonWhySearchTheseDepartments", "Test reason");
        expectedEntries.put("partnerName", "Partner name");
        expectedEntries.put("knowPartnerDateOfBirth", "Nac ydw");
        expectedEntries.put("partnerApproximateAge", "30");
        expectedEntries.put("knowPartnerNationalInsurance", "Ydw");
        expectedEntries.put("partnerNationalInsurance", "12345678");
        expectedEntries.put("partnerLastKnownAddress", "address1\nUK");
        expectedEntries.put("datesPartnerLivedAtLastKnownAddress", "Test date1");
        expectedEntries.put("knowAdditionalAddressesForPartner", "Ydw");
        expectedEntries.put("additionalAddress1", "Test address1");
        expectedEntries.put("additionalAddress1DatesLivedThere", "1 Jan 2023");
        expectedEntries.put("additionalAddress2", "Test address2");
        expectedEntries.put("additionalAddress2DatesLivedThere", "1 Jan 2024");
        expectedEntries.put("divorceAndDissolutionHeader","Divorce and Dissolution");
        expectedEntries.put("phoneAndOpeningTimes","0300 303 0642 (Monday to Friday, 10am to 6pm)");
        expectedEntries.put("courtsAndTribunalsServiceHeader","HM Courts & Tribunals Service");
        expectedEntries.put("contactEmail","contactdivorce@justice.gov.uk");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldGetSupportedTemplates() {
        assertThat(templateContent.getSupportedTemplates()).containsOnly(SEARCH_GOV_RECORDS_APPLICATION_TEMPLATE_ID);
    }

    private void buildSearchGovRecords(CaseData caseData, boolean knowPartnerDob) {
        caseData.getApplicant1().getInterimApplicationOptions().setSearchGovRecordsJourneyOptions(
            SearchGovRecordsJourneyOptions.builder()
                .whyTheseDepartments("Test reason")
                .knowPartnerNationalInsurance(YesOrNo.YES)
                .partnerNationalInsurance("12345678")
                .otherDepartmentNames("Test department")
                .partnerAdditionalAddress1("Test address1")
                .partnerAdditionalAddressDates1("1 Jan 2023")
                .partnerAdditionalAddress2("Test address2")
                .partnerAdditionalAddressDates2("1 Jan 2024")
                .knowPartnerAdditionalAddresses(YesOrNo.YES)
                .partnerName("Partner name")
                .whichDepartments(new LinkedHashSet<>(Set.of(SearchGovRecordsWhichDepartment.HMRC, SearchGovRecordsWhichDepartment.DWP,
                    SearchGovRecordsWhichDepartment.OTHER)))
                .partnerLastKnownAddress(AddressGlobalUK.builder().addressLine1("address1").country("UK").build())
                .partnerLastKnownAddressDates("Test date1")
                .reasonForApplying("Test reason")
                .build());

        if (knowPartnerDob) {
            caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
                .setKnowPartnerDateOfBirth(YesOrNo.YES);
            caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
                .setPartnerDateOfBirth(LocalDate.of(1990, 1, 1));
        } else {
            caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
                .setKnowPartnerDateOfBirth(YesOrNo.NO);
            caseData.getApplicant1().getInterimApplicationOptions().getSearchGovRecordsJourneyOptions()
                .setPartnerApproximateAge("30");
        }
    }
}
