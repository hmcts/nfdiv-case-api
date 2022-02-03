package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.content.provider.ApplicantTemplateDataProvider;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_GIVEN;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_IS_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LINE_1_LINE_2_CITY_POSTCODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class DraftDivorceApplicationSoleTemplateContentTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Mock
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    @InjectMocks
    private DraftApplicationTemplateContent draftApplicationTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForSoleDivorceApplication() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("Mr Sol")
                .email("sol@solbros.com")
                .organisationPolicy(
                    OrganisationPolicy.<UserRole>builder()
                        .organisation(
                            Organisation.builder()
                                .organisationName("Sol Bros")
                                .build()
                        )
                        .build())
                .address(LINE_1_LINE_2_CITY_POSTCODE)
                .build()
        );

        when(applicationTemplateDataProvider.deriveSoleJurisdictionList(any(), eq(TEST_CASE_ID)))
            .thenReturn(List.of(new ApplicationTemplateDataProvider.Connection(APP_1_APP_2_RESIDENT.getLabel())));
        when(applicantTemplateDataProvider.deriveApplicantPostalAddress(eq(caseData.getApplicant1())))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);
        when(applicantTemplateDataProvider.deriveApplicant2PostalAddress(eq(caseData.getApplicant2()), any()))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);

        Map<String, Object> templateContent = draftApplicationTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce from"),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(DIVORCE_OR_DISSOLUTION, DIVORCE_APPLICATION),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce"),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, false),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null),
            entry(RESPONDENT_IS_REPRESENTED, true),
            entry(RESPONDENT_SOLICITOR_NAME, "Mr Sol"),
            entry(RESPONDENT_SOLICITOR_EMAIL, "sol@solbros.com"),
            entry(RESPONDENT_SOLICITOR_FIRM_NAME, "Sol Bros"),
            entry(RESPONDENT_SOLICITOR_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE)
        );

        verifyNoMoreInteractions(authTokenGenerator);
        verify(applicantTemplateDataProvider).deriveApplicantPostalAddress(any(Applicant.class));
        verify(applicantTemplateDataProvider).deriveApplicant2PostalAddress(any(Applicant.class), any(Application.class));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForJointDivorceApplication() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setFinancialOrder(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );

        when(applicationTemplateDataProvider.deriveJointJurisdictionList(any(), eq(TEST_CASE_ID)))
            .thenReturn(List.of(new ApplicationTemplateDataProvider.Connection(APP_1_APP_2_RESIDENT.getLabel())));
        when(applicantTemplateDataProvider.deriveApplicantPostalAddress(eq(caseData.getApplicant1())))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);
        when(applicantTemplateDataProvider.deriveApplicant2PostalAddress(eq(caseData.getApplicant2()), any()))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);

        Map<String, Object> templateContent = draftApplicationTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_1_EMAIL, "test@test.com"),
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce"),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(DIVORCE_OR_DISSOLUTION, DIVORCE_APPLICATION),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce"),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_1, false),
            entry(HAS_FINANCIAL_ORDER_APPLICANT_2, true),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry(MARRIAGE_OR_RELATIONSHIP, MARRIAGE),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_MIDDLE_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null),
            entry(APPLICANT_2_EMAIL, null)
        );

        verifyNoMoreInteractions(authTokenGenerator);
        verify(applicantTemplateDataProvider).deriveApplicantPostalAddress(any(Applicant.class));
        verify(applicantTemplateDataProvider).deriveApplicant2PostalAddress(any(Applicant.class), any(Application.class));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForSoleApplicationForDissolution() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().build());

        when(applicationTemplateDataProvider.deriveSoleJurisdictionList(any(), eq(TEST_CASE_ID)))
            .thenReturn(List.of(new ApplicationTemplateDataProvider.Connection(APP_1_APP_2_RESIDENT.getLabel())));
        when(applicantTemplateDataProvider.deriveApplicantPostalAddress(eq(caseData.getApplicant1())))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);
        when(applicantTemplateDataProvider.deriveApplicant2PostalAddress(eq(caseData.getApplicant2()), any()))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);

        Map<String, Object> templateContent = draftApplicationTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_1_EMAIL, "test@test.com"),
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for the dissolution of the civil partnership with"),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null),
            entry(APPLICANT_2_EMAIL, null),
            entry(RESPONDENT_IS_REPRESENTED, true),
            entry(RESPONDENT_SOLICITOR_NAME, NOT_GIVEN),
            entry(RESPONDENT_SOLICITOR_EMAIL, NOT_GIVEN),
            entry(RESPONDENT_SOLICITOR_FIRM_NAME, NOT_GIVEN),
            entry(RESPONDENT_SOLICITOR_ADDRESS, NOT_GIVEN)
        );

        verifyNoMoreInteractions(authTokenGenerator);
        verify(applicantTemplateDataProvider).deriveApplicantPostalAddress(any(Applicant.class));
        verify(applicantTemplateDataProvider).deriveApplicant2PostalAddress(any(Applicant.class), any(Application.class));
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForJointApplicationForDissolution() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );

        when(applicationTemplateDataProvider.deriveJointJurisdictionList(any(), eq(TEST_CASE_ID)))
            .thenReturn(List.of(new ApplicationTemplateDataProvider.Connection(APP_1_APP_2_RESIDENT.getLabel())));
        when(applicantTemplateDataProvider.deriveApplicantPostalAddress(eq(caseData.getApplicant1())))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);
        when(applicantTemplateDataProvider.deriveApplicant2PostalAddress(eq(caseData.getApplicant2()), any()))
            .thenReturn(LINE_1_LINE_2_CITY_POSTCODE);

        Map<String, Object> templateContent = draftApplicationTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_FULL_NAME, null),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_1_MIDDLE_NAME, TEST_MIDDLE_NAME),
            entry(APPLICANT_1_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_1_EMAIL, "test@test.com"),
            entry(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for the dissolution of their civil partnership"),
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(DIVORCE_OR_DISSOLUTION, APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP),
            entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP),
            entry(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP),
            entry(MARRIAGE_DATE, null),
            entry(APPLICANT_2_POSTAL_ADDRESS, LINE_1_LINE_2_CITY_POSTCODE),
            entry(APPLICANT_2_FIRST_NAME, null),
            entry(APPLICANT_2_FULL_NAME, null),
            entry(APPLICANT_2_LAST_NAME, null),
            entry(APPLICANT_2_EMAIL, null)
        );

        verifyNoMoreInteractions(authTokenGenerator);
        verify(applicantTemplateDataProvider).deriveApplicantPostalAddress(any(Applicant.class));
        verify(applicantTemplateDataProvider).deriveApplicant2PostalAddress(any(Applicant.class), any(Application.class));
    }

    @Test
    public void shouldConvertMarriageDateToCorrectFormat() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(2019, 06, 4));
        marriageDetails.setPlaceOfMarriage("UK");

        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplication().setMarriageDetails(marriageDetails);

        when(applicationTemplateDataProvider.deriveSoleJurisdictionList(any(), eq(TEST_CASE_ID)))
            .thenReturn(List.of(new ApplicationTemplateDataProvider.Connection(APP_1_APP_2_RESIDENT.getLabel())));

        Map<String, Object> templateContent = draftApplicationTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(MARRIAGE_DATE, "4 June 2019"),
            entry(PLACE_OF_MARRIAGE, "UK")
        );

        verifyNoMoreInteractions(authTokenGenerator);
    }
}
