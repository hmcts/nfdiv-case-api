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
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.templatecontent.AosResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
public class AosResponseLetterTemplateContentTest {

    @Mock
    private Clock clock;

    @Mock
    private CommonContent commonContent;

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private AosResponseLetterTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyDivorceContent() {
        setMockClock(clock);
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .offline(YES)
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
            .firstName(TEST_APP2_FIRST_NAME)
            .lastName(TEST_APP2_LAST_NAME)
            .offline(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .dueDate(LocalDate.of(2020, 5, 21))
            .application(
                Application.builder().issueDate(LocalDate.of(2020, 1, 1)).build()
            )
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("husband");
        when(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()))
            .thenReturn(caseData.getApplication().getIssueDate().plusDays(141));
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("caseReference", formatId(TEST_CASE_ID));
        expectedEntries.put("applicant1FirstName", TEST_FIRST_NAME);
        expectedEntries.put("applicant1LastName", TEST_LAST_NAME);
        expectedEntries.put("applicant2FirstName", TEST_APP2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", TEST_APP2_LAST_NAME);
        expectedEntries.put("isDivorce", true);
        expectedEntries.put("applicant1Address", "Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nUK\nPost Code");
        expectedEntries.put("divorceOrCivilPartnershipEmail", "contactdivorce@justice.gov.uk");
        expectedEntries.put("divorceOrEndCivilPartnershipApplication", "divorce application");
        expectedEntries.put("issueDate", "1 January 2020");
        expectedEntries.put("relation", "husband");
        expectedEntries.put("waitUntilDate", "21 May 2020");
        expectedEntries.put("divorceOrEndCivilPartnershipProcess", "divorce process");
        expectedEntries.put("divorceOrCivilPartnershipProceedings", "divorce proceedings");
        expectedEntries.put("dueDate", "21 May 2020");
        expectedEntries.put("date", LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put("divorceOrCivilPartnershipServiceHeader", "The Divorce Service");
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionContent() {
        setMockClock(clock);
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .offline(YES)
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
            .firstName(TEST_APP2_FIRST_NAME)
            .lastName(TEST_APP2_LAST_NAME)
            .offline(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .dueDate(LocalDate.of(2020, 5, 21))
            .application(
                Application.builder().issueDate(LocalDate.of(2020, 1, 1)).build()
            )
            .build();

        when(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()))
            .thenReturn(caseData.getApplication().getIssueDate().plusDays(141));
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));


        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("caseReference", formatId(TEST_CASE_ID));
        expectedEntries.put("applicant1FirstName", TEST_FIRST_NAME);
        expectedEntries.put("applicant1LastName", TEST_LAST_NAME);
        expectedEntries.put("applicant1Address", "Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nUK\nPost Code");
        expectedEntries.put("divorceOrCivilPartnershipEmail", "contactdivorce@justice.gov.uk");
        expectedEntries.put("applicant2FirstName", TEST_APP2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", TEST_APP2_LAST_NAME);
        expectedEntries.put("isDivorce", false);
        expectedEntries.put("divorceOrEndCivilPartnershipApplication", "application to end your civil partnership");
        expectedEntries.put("issueDate", "1 January 2020");
        expectedEntries.put("relation", "civil partner");
        expectedEntries.put("waitUntilDate", "21 May 2020");
        expectedEntries.put("divorceOrEndCivilPartnershipProcess", "process to end your civil partnership");
        expectedEntries.put("divorceOrCivilPartnershipProceedings", "proceedings to end your civil partnership");
        expectedEntries.put("dueDate", "21 May 2020");
        expectedEntries.put("date", LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put("divorceOrCivilPartnershipServiceHeader", "End A Civil Partnership Service");
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyDivorceApp1SolicitorJSContent() {
        setMockClock(clock);
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .offline(YES)
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
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .name("App1Sol Name")
                .address("App1Sol Address")
                .reference("App1Sol Ref")
                .build())
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_APP2_FIRST_NAME)
            .lastName(TEST_APP2_LAST_NAME)
            .offline(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .dueDate(LocalDate.of(2020, 5, 21))
            .application(
                Application.builder().issueDate(LocalDate.of(2020, 1, 1)).build()
            )
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("husband");
        when(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()))
            .thenReturn(caseData.getApplication().getIssueDate().plusDays(141));
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference())).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("caseReference", formatId(TEST_CASE_ID));
        expectedEntries.put("applicant1FirstName", TEST_FIRST_NAME);
        expectedEntries.put("applicant1LastName", TEST_LAST_NAME);
        expectedEntries.put("applicant2FirstName", TEST_APP2_FIRST_NAME);
        expectedEntries.put("applicant2LastName", TEST_APP2_LAST_NAME);
        expectedEntries.put("isDivorce", true);
        expectedEntries.put("applicant1Address", "App1Sol Address");
        expectedEntries.put("divorceOrCivilPartnershipEmail", CONTACT_DIVORCE_EMAIL);
        expectedEntries.put("divorceOrEndCivilPartnershipApplication", "divorce application");
        expectedEntries.put("issueDate", "1 January 2020");
        expectedEntries.put("relation", "husband");
        expectedEntries.put("waitUntilDate", "21 May 2020");
        expectedEntries.put("divorceOrEndCivilPartnershipProcess", "divorce process");
        expectedEntries.put("divorceOrCivilPartnershipProceedings", "divorce proceedings");
        expectedEntries.put("dueDate", "21 May 2020");
        expectedEntries.put("date", LocalDate.now().format(DATE_TIME_FORMATTER));
        expectedEntries.put("divorceOrCivilPartnershipServiceHeader", "The Divorce Service");
        expectedEntries.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        expectedEntries.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        expectedEntries.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        expectedEntries.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        expectedEntries.put(RECIPIENT_NAME, "App1Sol Name");
        expectedEntries.put(RECIPIENT_ADDRESS, "App1Sol Address");
        expectedEntries.put(SOLICITOR_NAME, "App1Sol Name");
        expectedEntries.put(APPLICANT_2_SOLICITOR_NAME, "Not represented");
        expectedEntries.put(SOLICITOR_REFERENCE, "App1Sol Ref");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
