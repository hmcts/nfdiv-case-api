package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent.APPLICANT_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent.APPLICANT_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderTemplateContent.END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FAMILY_COURT_LOGO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HMCTS_LOGO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.MARRIED_TO_MORE_THAN_ONE_PERSON;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ConditionalOrderReminderTemplateContentIT {

    private static final String HMCTS_LOGO_TEXT = "[userImage:hmcts_logo_nfd_en.png]";
    private static final String FAMILY_COURT_LOGO_TEXT = "[userImage:family_court_logo_nfd_en.png]";
    private static final String HMCTS_LOGO_TEXT_CY = "[userImage:hmcts_logo_nfd_cy.png]";
    private static final String FAMILY_COURT_LOGO_TEXT_CY = "[userImage:family_court_logo_nfd_cy.png]";

    @Autowired
    private ConditionalOrderReminderTemplateContent conditionalOrderReminderTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForConditionalOrderReminderDocumentDivorceSole() {

        CaseData caseData = caseData();

        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(applicantAddress());
        caseData.getApplicant2().setGender(FEMALE);

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .emailAddress("contactdivorce@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .phoneNumber("0300 303 0642")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .emailAddress("contactdivorce@justice.gov.uk")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(APPLICANT_FIRST_NAME, caseData.getApplicant1().getFirstName());
        expectedEntries.put(APPLICANT_LAST_NAME, caseData.getApplicant1().getLastName());
        expectedEntries.put(APPLICANT_ADDRESS, caseData.getApplicant1().getAddress());
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(IS_JOINT, !caseData.getApplicationType().isSole());
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        expectedEntries.put(CTSC_CONTACT_DETAILS, ctscContactDetails);
        expectedEntries.put(HMCTS_LOGO,  HMCTS_LOGO_TEXT);
        expectedEntries.put(FAMILY_COURT_LOGO, FAMILY_COURT_LOGO_TEXT);

        Map<String, Object> templateContent = conditionalOrderReminderTemplateContent.getTemplateContent(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForConditionalOrderReminderDocumentCivilPartnershipJoint() {

        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(applicantAddress());
        caseData.getApplicant2().setGender(MALE);

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .emailAddress("contactdivorce@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .phoneNumber("0300 303 0642")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .emailAddress("contactdivorce@justice.gov.uk")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(APPLICANT_FIRST_NAME, caseData.getApplicant1().getFirstName());
        expectedEntries.put(APPLICANT_LAST_NAME, caseData.getApplicant1().getLastName());
        expectedEntries.put(APPLICANT_ADDRESS, caseData.getApplicant1().getAddress());
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, TO_END_A_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, END_THE_CIVIL_PARTNERSHIP);
        expectedEntries.put(IS_JOINT, !caseData.getApplicationType().isSole());
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        expectedEntries.put(CTSC_CONTACT_DETAILS, ctscContactDetails);
        expectedEntries.put(HMCTS_LOGO,  HMCTS_LOGO_TEXT);
        expectedEntries.put(FAMILY_COURT_LOGO, FAMILY_COURT_LOGO_TEXT);

        Map<String, Object> templateContent = conditionalOrderReminderTemplateContent.getTemplateContent(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    private AddressGlobalUK applicantAddress() {
        return AddressGlobalUK.builder()
            .addressLine1("223b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();
    }
}
