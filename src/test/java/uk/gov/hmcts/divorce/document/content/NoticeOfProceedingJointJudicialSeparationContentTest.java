package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.HAS_CASE_BEEN_REISSUED;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.REISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingJointJudicialSeparationContentTest {

    private static final LocalDate APPLICATION_REISSUE_DATE = LocalDate.of(2022, 4, 30);


    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private NoticeOfProceedingJointJudicialSeparationContent jointJudicialSeparationContent;


    @Test
    public void shouldSuccessfullyApplyDivorceContent() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(NO)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build()
            )
            .build();

        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(MALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(
                Application.builder().issueDate(LocalDate.of(2020, 1, 1)).build()
            )
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), applicant1.getLanguagePreference())).thenReturn("husband");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> result = jointJudicialSeparationContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            caseData.getApplicant2());

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ADDRESS, "Correspondence Address\nLine 2\nLine 3\nPost Town\nPost Code");
        expectedEntries.put(ISSUE_DATE, "1 January 2020");
        expectedEntries.put(PARTNER, "husband");
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionContent() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(NO)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build()
            )
            .build();

        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(MALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(
                Application.builder().issueDate(LocalDate.of(2020, 1, 1)).build()
            )
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), applicant1.getLanguagePreference())).thenReturn("civil partner");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> result = jointJudicialSeparationContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            caseData.getApplicant2());

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ADDRESS, "Correspondence Address\nLine 2\nLine 3\nPost Town\nPost Code");
        expectedEntries.put(ISSUE_DATE, "1 January 2020");
        expectedEntries.put(PARTNER, "civil partner");
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }



    @Test
    public void shouldSuccessfullyShowReissueDateContent() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(NO)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build()
            )
            .build();

        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(MALE)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(Application.builder()
                .issueDate(LocalDate.of(2020,1,1))
                .reissueDate(LocalDate.of(2020,1,1))
                .build())
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), applicant1.getLanguagePreference())).thenReturn("civil partner");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> result = jointJudicialSeparationContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(),
            caseData.getApplicant2());

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedEntries.put(FIRST_NAME, TEST_FIRST_NAME);
        expectedEntries.put(LAST_NAME, TEST_LAST_NAME);
        expectedEntries.put(ADDRESS, "Correspondence Address\nLine 2\nLine 3\nPost Town\nPost Code");
        expectedEntries.put(ISSUE_DATE, "1 January 2020");
        expectedEntries.put(HAS_CASE_BEEN_REISSUED, true);
        expectedEntries.put(REISSUE_DATE, "1 January 2020");
        expectedEntries.put(PARTNER, "civil partner");
        expectedEntries.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }


}
