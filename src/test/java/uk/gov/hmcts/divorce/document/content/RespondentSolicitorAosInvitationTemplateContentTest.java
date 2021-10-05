package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LINE_1_LINE_2_CITY_POSTCODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
public class RespondentSolicitorAosInvitationTemplateContentTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private RespondentSolicitorAosInvitationTemplateContent respondentSolicitorAosInvitationTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForDivorce() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );

        Map<String, Object> templateContent = respondentSolicitorAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null)
        );

        verifyNoMoreInteractions(authTokenGenerator);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForDissolution() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );

        Map<String, Object> templateContent = respondentSolicitorAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(ISSUE_DATE, "28 April 2021"),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null)
        );

        verifyNoMoreInteractions(authTokenGenerator);
    }

    @Test
    public void shouldConvertMarriageDateToCorrectFormat() {
        CaseData caseData = caseData();
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(2019, 06, 4));

        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplication().setMarriageDetails(marriageDetails);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );

        Map<String, Object> templateContent = respondentSolicitorAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContent).contains(
            entry(MARRIAGE_DATE, "4 June 2019")
        );

        verifyNoMoreInteractions(authTokenGenerator);
    }

    @Test
    public void shouldSuccessfullyApplyApplicant2PostalAddressIfApplicant2HomeAddressNotNull() {
        AddressGlobalUK address = AddressGlobalUK.builder()
            .addressLine1("221b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();

        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setHomeAddress(address);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setFinancialOrder(NO);

        Map<String, Object> templateContent = respondentSolicitorAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContent).contains(
            entry(APPLICANT_2_POSTAL_ADDRESS, "221b\nBaker Street\nLondon\nGreater London\nNW1 6XE\nUnited Kingdom")
        );
    }
}
