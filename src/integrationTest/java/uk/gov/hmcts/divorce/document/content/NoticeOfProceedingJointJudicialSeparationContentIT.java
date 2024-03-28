package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.JUDICIAL_SEPARATION_PROCEEDINGS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.MARRIED_TO_MORE_THAN_ONE_PERSON;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.REISSUED_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NoticeOfProceedingJointJudicialSeparationContentIT {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private NoticeOfProceedingJointJudicialSeparationContent noticeOfProceedingContentJudicialSeparation;

    public static final String MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT = "You must tell the court if you’ve been married to more than"
        + " one person during this marriage.";

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingNoticeOfProceedingJointDocForJudicialSeparation() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
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
        caseData.getApplication().setReissueDate(LocalDate.of(2021, 7, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));

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
        expectedEntries.put(CASE_REFERENCE, formatId(1616591401473378L));
        expectedEntries.put(FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ISSUE_DATE, "18 June 2021");
        expectedEntries.put(REISSUED_DATE, "Reissued on: 18 July 2021");
        expectedEntries.put(ADDRESS, "line 1\ntown\nUK\npostcode");
        expectedEntries.put(RELATION, "wife");
        expectedEntries.put(JUDICIAL_SEPARATION_PROCEEDINGS, "judicial separation proceedings");
        expectedEntries.put(JUDICIAL_SEPARATION, "judicial separation");
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        expectedEntries.put(MARRIED_TO_MORE_THAN_ONE_PERSON, MARRIED_TO_MORE_THAN_ONE_PERSON_TEXT);
        expectedEntries.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        Map<String, Object> templateContent =
            noticeOfProceedingContentJudicialSeparation.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
