package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_REGISTERED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPOND_BY_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME_WITH_DEFAULT_VALUE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.WHO_APPLIED;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_2_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.BEEN_MARRIED_TO;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CAN_SERVE_BY_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DISPLAY_EMAIL_CONFIRMATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_URL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_PAPERS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_PROCEEDINGS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_URL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.END_A_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENTERED_INTO_A_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.HAS_CASE_BEEN_REISSUED;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_COURT_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_RESPONDENT_BASED_IN_UK;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE_OR_CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PAPERS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.REISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.RELATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.RELATIONS_SOLICITOR;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.SERVE_PAPERS_BEFORE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.THE_DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.TO_END_THEIR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.URL_TO_LINK_CASE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NoticeOfProceedingContentIT {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisTemplatesConfig config;

    @Autowired
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingNoticeOfProceedingDocumentForDivorceApplication() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setFirstName(APPLICANT_2_FIRST_NAME);
        caseData.getApplicant2().setLastName(APPLICANT_2_LAST_NAME);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .build()
        );
        caseData.getApplicant2().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("10 the street")
                .addressLine2("the town")
                .country("UK")
                .build()
        );
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));
        caseData.setCaseInvite(
            new CaseInvite("app2@email.com", "ACCESS_CODE", "app2_id")
        );

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(DUE_DATE, "19 June 2021");
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(RELATION, "wife");
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
        expectedEntries.put(SUBMISSION_RESPONSE_DATE, "6 November 2021");
        expectedEntries.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS);
        expectedEntries.put(SERVE_PAPERS_BEFORE_DATE, "16 July 2021");
        expectedEntries.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, BEEN_MARRIED_TO);
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);
        expectedEntries.put(APPLICANT_1_ADDRESS, "line1\nline2");
        expectedEntries.put(APPLICANT_2_ADDRESS, "10 the street\nthe town");
        expectedEntries.put(APPLICANT_1_SOLICITOR_NAME, "Not represented");
        expectedEntries.put(DISPLAY_EMAIL_CONFIRMATION, true);
        expectedEntries.put("applicant2FirstName", APPLICANT_2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", APPLICANT_2_LAST_NAME);
        expectedEntries.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(RESPOND_BY_DATE, "4 July 2021");
        expectedEntries.put(IS_COURT_SERVICE, false);
        expectedEntries.put(ACCESS_CODE, "ACCESS_CODE");
        expectedEntries.put(URL_TO_LINK_CASE, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net/applicant2");
        expectedEntries.put(CAN_SERVE_BY_EMAIL, true);
        expectedEntries.put(IS_RESPONDENT_BASED_IN_UK, true);

        Map<String, Object> templateContent = noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }


    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingNoticeOfProceedingDocumentForDissolution() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .build()
        );
        caseData.getApplicant2().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("10 the street")
                .addressLine2("the town")
                .country("UK")
                .build()
        );
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setFirstName(APPLICANT_2_FIRST_NAME);
        caseData.getApplicant2().setLastName(APPLICANT_2_LAST_NAME);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));
        caseData.setCaseInvite(
            new CaseInvite("app2@email.com", "ACCESS_CODE", "app2_id")
        );

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(DUE_DATE, "19 June 2021");
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(RELATION, CIVIL_PARTNER);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, ENDING_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, CIVIL_PARTNERSHIP_EMAIL);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, END_A_CIVIL_PARTNERSHIP_SERVICE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_A_CIVIL_PARTNERSHIP_SERVICE);
        expectedEntries.put(SUBMISSION_RESPONSE_DATE, "6 November 2021");
        expectedEntries.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, ENDING_A_CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, PAPERS_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(SERVE_PAPERS_BEFORE_DATE, "16 July 2021");
        expectedEntries.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, ENTERED_INTO_A_CIVIL_PARTNERSHIP_WITH);
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNER, CIVIL_PARTNERSHIP);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);
        expectedEntries.put(APPLICANT_1_ADDRESS, "line1\nline2");
        expectedEntries.put(APPLICANT_2_ADDRESS, "10 the street\nthe town");
        expectedEntries.put(APPLICANT_1_SOLICITOR_NAME, "Not represented");
        expectedEntries.put(DISPLAY_EMAIL_CONFIRMATION, true);
        expectedEntries.put("applicant2FirstName", APPLICANT_2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", APPLICANT_2_LAST_NAME);
        expectedEntries.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, TO_END_THEIR_CIVIL_PARTNERSHIP);
        expectedEntries.put(RESPOND_BY_DATE, "4 July 2021");
        expectedEntries.put(IS_COURT_SERVICE, false);
        expectedEntries.put(ACCESS_CODE, "ACCESS_CODE");
        expectedEntries.put(URL_TO_LINK_CASE, "https://nfdiv-end-civil-partnership.aat.platform.hmcts.net/applicant2");
        expectedEntries.put(CAN_SERVE_BY_EMAIL, true);
        expectedEntries.put(IS_RESPONDENT_BASED_IN_UK, true);

        Map<String, Object> templateContent = noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingNoticeOfProceedingDocumentRepresentedApplicant2() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setFirstName(APPLICANT_2_FIRST_NAME);
        caseData.getApplicant2().setLastName(APPLICANT_2_LAST_NAME);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .build()
        );
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));

        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("app 1 sol")
                .address("line1")
                .reference("ref")
                .build()
        );
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("app 2 sol")
                .address("The avenue")
                .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                    .build())
                .build()
        );
        caseData.setCaseInvite(
            new CaseInvite("app2@email.com", "ACCESS_CODE", "app2_id")
        );

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(DUE_DATE, "19 June 2021");
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(RELATION, "wife");
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
        expectedEntries.put(SUBMISSION_RESPONSE_DATE, "6 November 2021");
        expectedEntries.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS);
        expectedEntries.put(SERVE_PAPERS_BEFORE_DATE, "16 July 2021");
        expectedEntries.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, BEEN_MARRIED_TO);
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);
        expectedEntries.put(APPLICANT_1_ADDRESS, "line1");
        expectedEntries.put(APPLICANT_2_ADDRESS, "The avenue");
        expectedEntries.put(APPLICANT_1_SOLICITOR_NAME, "app 1 sol");
        expectedEntries.put(DISPLAY_EMAIL_CONFIRMATION, true);
        expectedEntries.put("applicant2FirstName", APPLICANT_2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", APPLICANT_2_LAST_NAME);
        expectedEntries.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(SOLICITOR_NAME, "app 2 sol");
        expectedEntries.put(SOLICITOR_ADDRESS, "The avenue");
        expectedEntries.put(SOLICITOR_REFERENCE, "ref");
        expectedEntries.put(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "app 1 sol");
        expectedEntries.put(WHO_APPLIED, "applicant's solicitor");
        expectedEntries.put(RESPOND_BY_DATE, "4 July 2021");
        expectedEntries.put(RESPONDENT_SOLICITOR_REGISTERED, "Yes");
        expectedEntries.put(IS_COURT_SERVICE, false);
        expectedEntries.put(ACCESS_CODE, "ACCESS_CODE");
        expectedEntries.put(URL_TO_LINK_CASE, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net/applicant2");
        expectedEntries.put(CAN_SERVE_BY_EMAIL, true);
        expectedEntries.put(IS_RESPONDENT_BASED_IN_UK, true);
        expectedEntries.put(IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE, false);

        Map<String, Object> templateContent = noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataNotCourtServiceAndReissued() {
        CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.getApplication().setReissueDate(LocalDate.of(2021, 6, 18));
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setFirstName(APPLICANT_2_FIRST_NAME);
        caseData.getApplicant2().setLastName(APPLICANT_2_LAST_NAME);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .build()
        );
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));
        caseData.setCaseInvite(
            new CaseInvite("app2@email.com", "ACCESS_CODE", "app2_id")
        );
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("app 1 sol")
                .address("line1")
                .reference("ref")
                .build()
        );
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("app 2 sol")
                .address("The avenue")
                .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                    .build())
                .build()
        );

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(DUE_DATE, "19 June 2021");
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(RELATION, "wife");
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
        expectedEntries.put(SUBMISSION_RESPONSE_DATE, "6 November 2021");
        expectedEntries.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS);
        expectedEntries.put(SERVE_PAPERS_BEFORE_DATE, "16 July 2021");
        expectedEntries.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, BEEN_MARRIED_TO);
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);
        expectedEntries.put(APPLICANT_1_ADDRESS, "line1");
        expectedEntries.put(APPLICANT_2_ADDRESS, "The avenue");
        expectedEntries.put(APPLICANT_1_SOLICITOR_NAME, "app 1 sol");
        expectedEntries.put(DISPLAY_EMAIL_CONFIRMATION, true);
        expectedEntries.put("applicant2FirstName", APPLICANT_2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", APPLICANT_2_LAST_NAME);
        expectedEntries.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(SOLICITOR_NAME, "app 2 sol");
        expectedEntries.put(SOLICITOR_ADDRESS, "The avenue");
        expectedEntries.put(SOLICITOR_REFERENCE, "ref");
        expectedEntries.put(SOLICITOR_NAME_WITH_DEFAULT_VALUE, "app 1 sol");
        expectedEntries.put(WHO_APPLIED, "applicant's solicitor");
        expectedEntries.put(RESPOND_BY_DATE, "4 July 2021");
        expectedEntries.put(RESPONDENT_SOLICITOR_REGISTERED, "Yes");
        expectedEntries.put(REISSUE_DATE, "18 June 2021");
        expectedEntries.put(HAS_CASE_BEEN_REISSUED, true);
        expectedEntries.put(IS_COURT_SERVICE, false);
        expectedEntries.put(ACCESS_CODE, "ACCESS_CODE");
        expectedEntries.put(URL_TO_LINK_CASE, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net/applicant2");
        expectedEntries.put(CAN_SERVE_BY_EMAIL, true);
        expectedEntries.put(IS_RESPONDENT_BASED_IN_UK, true);
        expectedEntries.put(RELATIONS_SOLICITOR, "wife's solicitor");
        expectedEntries.put(IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE, true);


        Map<String, Object> templateContent = noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyRepresentedApplicant2ContentIfDataMissing() {
        CaseData caseData = caseData();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setFirstName(APPLICANT_2_FIRST_NAME);
        caseData.getApplicant2().setLastName(APPLICANT_2_LAST_NAME);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .build()
        );
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));

        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .build()
        );
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("app 2 sol")
                .address("The avenue")
                .build()
        );
        caseData.setCaseInvite(
            new CaseInvite("app2@email.com", "ACCESS_CODE", "app2_id")
        );

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(APPLICANT_1_LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(DUE_DATE, "19 June 2021");
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(RELATION, "wife");
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
        expectedEntries.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE);
        expectedEntries.put(SUBMISSION_RESPONSE_DATE, "6 November 2021");
        expectedEntries.put(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS);
        expectedEntries.put(SERVE_PAPERS_BEFORE_DATE, "16 July 2021");
        expectedEntries.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, BEEN_MARRIED_TO);
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);
        expectedEntries.put(APPLICANT_1_ADDRESS, "line1\nline2");
        expectedEntries.put(APPLICANT_2_ADDRESS, "The avenue");
        expectedEntries.put(APPLICANT_1_SOLICITOR_NAME, "Not represented");
        expectedEntries.put(DISPLAY_EMAIL_CONFIRMATION, true);
        expectedEntries.put("applicant2FirstName", APPLICANT_2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", APPLICANT_2_LAST_NAME);
        expectedEntries.put(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, FOR_A_DIVORCE);
        expectedEntries.put(SOLICITOR_NAME, "app 2 sol");
        expectedEntries.put(SOLICITOR_ADDRESS, "The avenue");
        expectedEntries.put(SOLICITOR_REFERENCE, NOT_PROVIDED);
        expectedEntries.put(SOLICITOR_NAME_WITH_DEFAULT_VALUE, NOT_REPRESENTED);
        expectedEntries.put(WHO_APPLIED, "applicant");
        expectedEntries.put(RESPOND_BY_DATE, "4 July 2021");
        expectedEntries.put(RESPONDENT_SOLICITOR_REGISTERED, "No");
        expectedEntries.put(IS_COURT_SERVICE, true);
        expectedEntries.put(ACCESS_CODE, "ACCESS_CODE");
        expectedEntries.put(URL_TO_LINK_CASE, "https://nfdiv-apply-for-divorce.aat.platform.hmcts.net/applicant2");
        expectedEntries.put(CAN_SERVE_BY_EMAIL, true);
        expectedEntries.put(IS_RESPONDENT_BASED_IN_UK, true);
        expectedEntries.put(IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE, false);

        Map<String, Object> templateContent = noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
