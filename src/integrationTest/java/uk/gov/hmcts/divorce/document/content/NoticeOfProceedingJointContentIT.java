package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DISPLAY_EMAIL_CONFIRMATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_URL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_PROCEEDINGS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_URL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.END_A_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE_OR_CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.RELATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.THE_DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent.ADDRESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NoticeOfProceedingJointContentIT {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private NoticeOfProceedingJointContent noticeOfProceedingContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingNoticeOfProceedingJointDocumentForDivorceApplication() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK.builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .country("UK")
                .build()
        );
        caseData.getApplicant2().setGender(FEMALE);

        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));

        var ctscContactDetails = CtscContactDetails
            .builder()
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(DUE_DATE, "19 June 2021");
        expectedEntries.put(ADDRESS, "line 1\ntown\npostcode");
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
        expectedEntries.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE);
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);
        expectedEntries.put(DISPLAY_EMAIL_CONFIRMATION, true);

        Map<String, Object> templateContent =
            noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingNoticeOfProceedingDocumentForDissolution() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setAddress(
            AddressGlobalUK.builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .country("UK")
                .build()
        );
        caseData.getApplicant1().setGender(FEMALE);

        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));


        var ctscContactDetails = CtscContactDetails
            .builder()
            .phoneNumber("0300 303 0642")
            .build();

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(DUE_DATE, "19 June 2021");
        expectedEntries.put(ADDRESS, "line 1\ntown\npostcode");
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
        expectedEntries.put(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP);
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNER, CIVIL_PARTNERSHIP);
        expectedEntries.put("ctscContactDetails", ctscContactDetails);
        expectedEntries.put(DISPLAY_EMAIL_CONFIRMATION, true);

        Map<String, Object> templateContent =
            noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSetRelationToSpouseIfPartnerGenderIsNotSet() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setAddress(
            AddressGlobalUK.builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .country("UK")
                .build()
        );
        caseData.getApplicant1().setGender(null);

        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(RELATION, "spouse");

        Map<String, Object> templateContent =
            noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());

        assertThat(templateContent).containsAllEntriesOf(expectedEntries);
    }
}
