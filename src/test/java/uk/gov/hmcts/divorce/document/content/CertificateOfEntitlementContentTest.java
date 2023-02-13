package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.ConditionalOrderCourtDetails;
import uk.gov.hmcts.divorce.common.config.ConditionalOrderCourtDetailsConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
public class CertificateOfEntitlementContentTest {

    @Mock
    private ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private CertificateOfEntitlementContent certificateOfEntitlementContent;

    @Test
    void shouldReturnTemplateContentForSole() {

        final CaseData caseData = getCaseDataFor(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        final ConditionalOrderCourtDetails expectedDetails = setupConditionalOrderCourtDetailsConfig();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
                caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", expectedDetails),
            entry("approvalDate", "8 November 2021"),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry("isSole", true),
            entry("isJoint", false),
            entry(DATE_OF_HEARING, "8 November 2021"),
            entry(TIME_OF_HEARING, "14:56 pm"),
            entry(BEFORE_DATE_OF_HEARING, "1 November 2021"),
            entry(HAS_FINANCIAL_ORDERS, true),
            entry(CTSC_CONTACT_DETAILS, buildCtscContactDetails()),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, "Divorce and Dissolution"),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, "HM Courts & Tribunals Service"),
            entry(CONTACT_EMAIL, "contactdivorce@justice.gov.uk"),
            entry(PHONE_AND_OPENING_TIMES, "Phone: 0300 303 0642 (Monday to Friday, 8am to 6pm)")
        );
    }

    @Test
    void shouldReturnTemplateContentForJoint() {

        final CaseData caseData = getCaseDataFor(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        final ConditionalOrderCourtDetails expectedDetails = setupConditionalOrderCourtDetailsConfig();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
                caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", expectedDetails),
            entry("approvalDate", "8 November 2021"),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry("isSole", false),
            entry("isJoint", true),
            entry(DATE_OF_HEARING, "8 November 2021"),
            entry(TIME_OF_HEARING, "14:56 pm"),
            entry(BEFORE_DATE_OF_HEARING, "1 November 2021"),
            entry(HAS_FINANCIAL_ORDERS, true),
            entry(CTSC_CONTACT_DETAILS, buildCtscContactDetails()),
            entry(DIVORCE_AND_DISSOLUTION_HEADER, "Divorce and Dissolution"),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, "HM Courts & Tribunals Service"),
            entry(CONTACT_EMAIL, "contactdivorce@justice.gov.uk"),
            entry(PHONE_AND_OPENING_TIMES, "Phone: 0300 303 0642 (Monday to Friday, 8am to 6pm)")
        );
    }

    @Test
    void shouldReturnWelshTemplateContentForDivorceIfLanguagePreferenceIsWelsh() {

        final CaseData caseData = getCaseDataFor(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE_CY)
        );
    }

    @Test
    void shouldReturnTemplateContentForCivilPartnership() {

        final CaseData caseData = getCaseDataFor(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP)
        );
    }

    @Test
    void shouldReturnWelshTemplateContentForCivilPartnershipIfLanguagePreferenceIsWelsh() {

        final CaseData caseData = getCaseDataFor(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP_CY)
        );
    }

    @Test
    void shouldSetHasFinancialOrdersAndCostsGrantedToFalseIfNotSet() {

        final CaseData caseData = getCaseDataFor(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setFinancialOrder(null);
        caseData.getConditionalOrder().setClaimsGranted(null);

        final ConditionalOrderCourtDetails expectedDetails = setupConditionalOrderCourtDetailsConfig();
        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", expectedDetails),
            entry("approvalDate", "8 November 2021"),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry("isSole", true),
            entry("isJoint", false),
            entry(DATE_OF_HEARING, "8 November 2021"),
            entry(TIME_OF_HEARING, "14:56 pm"),
            entry(BEFORE_DATE_OF_HEARING, "1 November 2021"),
            entry(HAS_FINANCIAL_ORDERS, false)
        );
    }

    @Test
    void shouldSetValuesToNullIfNotSetInCaseData() {

        final CaseData caseData = getCaseDataFor(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setFinancialOrder(null);
        caseData.getConditionalOrder().setClaimsGranted(null);
        caseData.getConditionalOrder().setCourt(null);
        caseData.getConditionalOrder().setDecisionDate(null);
        caseData.getConditionalOrder().setDateAndTimeOfHearing(null);

        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", null),
            entry("approvalDate", null),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry("isSole", true),
            entry("isJoint", false),
            entry(DATE_OF_HEARING, null),
            entry(TIME_OF_HEARING, null),
            entry(BEFORE_DATE_OF_HEARING, null),
            entry(HAS_FINANCIAL_ORDERS, false)
        );
    }

    private CaseData getCaseDataFor(final ApplicationType soleApplication) {
        final LocalDateTime localDateTime = LocalDateTime.of(2021, 11, 8, 14, 56);
        final LocalDate localDate = LocalDate.of(2021, 11, 8);
        return CaseData.builder()
            .applicationType(soleApplication)
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .financialOrder(YES)
                .gender(FEMALE)
                .build())
            .applicant2(Applicant.builder()
                .firstName("Jane")
                .lastName("Jones")
                .gender(MALE)
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .court(BURY_ST_EDMUNDS)
                .dateAndTimeOfHearing(localDateTime)
                .decisionDate(localDate)
                .claimsGranted(YES)
                .claimsCostsOrderInformation("info")
                .build())
            .application(Application.builder()
                .divorceWho(HUSBAND)
                .build())
            .build();
    }

    private ConditionalOrderCourtDetails setupConditionalOrderCourtDetailsConfig() {
        final ConditionalOrderCourtDetails expectedDetails = new ConditionalOrderCourtDetails();
        expectedDetails.setName("Bury St. Edmunds Regional Divorce Centre");
        expectedDetails.setAddress("2nd Floor\nTriton House\nSt. Andrews Street North\nBury St. Edmunds\nIP33 1TR");
        expectedDetails.setEmail("contactdivorce@justice.gov.uk");
        expectedDetails.setPhone("0300 303 0642");

        when(conditionalOrderCourtDetailsConfig.get(BURY_ST_EDMUNDS.getCourtId())).thenReturn(expectedDetails);
        return expectedDetails;
    }

    private CtscContactDetails buildCtscContactDetails() {
        return CtscContactDetails
                .builder()
                .centreName("HMCTS Digital Divorce and Dissolution")
                .serviceCentre("Courts and Tribunals Service Centre")
                .poBox("PO Box 13226")
                .town("Harlow")
                .postcode("CM20 9UG")
                .phoneNumber("0300 303 0642")
                .emailAddress("contactdivorce@justice.gov.uk")
                .build();
    }
}
