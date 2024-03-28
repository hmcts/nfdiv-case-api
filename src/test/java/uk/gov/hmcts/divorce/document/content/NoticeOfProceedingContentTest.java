package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.common.exception.InvalidCcdCaseDataException;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPOND_BY_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_2_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_2_IS_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.BEEN_MARRIED_TO;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CAN_SERVE_BY_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP_DOCUMENTS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DISPLAY_EMAIL_CONFIRMATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_DOCUMENTS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_DOCUMENTS;
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
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_URL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.END_A_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENTERED_INTO_A_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_COURT_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_OFFLINE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_RESPONDENT_BASED_IN_UK;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE_OR_CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PAPERS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.RELATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.RELATIONS_SOLICITOR;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.SERVE_PAPERS_BEFORE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.THE_DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.TO_END_THEIR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingContentTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisTemplatesConfig config;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @Test
    public void shouldSuccessfullyApplyDivorceContentForNoticeOfProceedings() {
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
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .address("10 the street the town UK")
                .build()
        );
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));
        caseData.setCaseInvite(
            new CaseInvite("app2@email.com", "ACCESS_CODE", "app2_id")
        );

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");
        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("wife");
        when(holdingPeriodService.getDueDateFor(LocalDate.of(2021, 6, 18)))
            .thenReturn(LocalDate.of(2021, 11, 6));

        Map<String, Object> templateContent = noticeOfProceedingContent.apply(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            ENGLISH);

        assertThat(templateContent)
            .contains(
                entry(CASE_REFERENCE, formatId(1616591401473378L)),
                entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
                entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
                entry(ISSUE_DATE, "18 June 2021"),
                entry(DUE_DATE, "19 June 2021"),
                entry(RELATIONS_SOLICITOR, "wife's solicitor"),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, DIVORCE_PROCEEDINGS),
                entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, FOR_A_DIVORCE),
                entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION),
                entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, DIVORCE_PROCESS),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_DIVORCE),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, DIVORCE_URL),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, DIVORCE_SERVICE),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, THE_DIVORCE_SERVICE),
                entry(SUBMISSION_RESPONSE_DATE, "6 November 2021"),
                entry(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, DIVORCE),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, DIVORCE_PAPERS),
                entry(SERVE_PAPERS_BEFORE_DATE, "16 July 2021"),
                entry(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE),
                entry(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, BEEN_MARRIED_TO),
                entry(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE),
                entry("ctscContactDetails", getCtscContactDetails()),
                entry(APPLICANT_1_ADDRESS, "line1\nline2\nUK"),
                entry(APPLICANT_2_ADDRESS, "10 the street the town UK"),
                entry(APPLICANT_1_SOLICITOR_NAME, "Not represented"),
                entry(DISPLAY_EMAIL_CONFIRMATION, true),
                entry("applicant2FirstName", APPLICANT_2_FIRST_NAME),
                entry("applicant2LastName", APPLICANT_2_LAST_NAME),
                entry(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, FOR_A_DIVORCE),
                entry(RESPOND_BY_DATE, "4 July 2021"),
                entry(IS_COURT_SERVICE, false),
                entry(IS_PERSONAL_SERVICE, true),
                entry(ACCESS_CODE, "ACCESS_CODE"),
                entry(CAN_SERVE_BY_EMAIL, true),
                entry(IS_RESPONDENT_BASED_IN_UK, true),
                entry(IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE, true),
                entry(IS_DIVORCE, true),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_DOCUMENTS, DIVORCE_DOCUMENTS),
                entry(IS_OFFLINE, false),
                entry(APPLICANT_2_IS_REPRESENTED, true)
            );
    }

    @Test
    public void shouldSuccessfullyApplyCivilPartnershipContentForNoticeOfProceedings() {
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

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn(CIVIL_PARTNER);
        when(holdingPeriodService.getDueDateFor(LocalDate.of(2021, 6, 18)))
            .thenReturn(LocalDate.of(2021, 11, 6));

        Map<String, Object> templateContent = noticeOfProceedingContent.apply(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            ENGLISH);

        assertThat(templateContent)
            .contains(
                entry(CASE_REFERENCE, formatId(1616591401473378L)),
                entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
                entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
                entry(ISSUE_DATE, "18 June 2021"),
                entry(DUE_DATE, "19 June 2021"),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_PROCEEDINGS, PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP),
                entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_YOUR_CIVIL_PARTNERSHIP),
                entry(RELATION, CIVIL_PARTNER),
                entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP),
                entry(DIVORCE_OR_END_CIVIL_PARTNERSHIP_PROCESS, PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_APPLICATION, YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP, ENDING_YOUR_CIVIL_PARTNERSHIP),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_URL, CIVIL_PARTNERSHIP_EMAIL),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE, END_A_CIVIL_PARTNERSHIP_SERVICE),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_SERVICE_HEADER, END_A_CIVIL_PARTNERSHIP_SERVICE),
                entry(SUBMISSION_RESPONSE_DATE, "6 November 2021"),
                entry(DIVORCE_OR_END_A_CIVIL_PARTNERSHIP, ENDING_A_CIVIL_PARTNERSHIP),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_PAPERS, PAPERS_TO_END_YOUR_CIVIL_PARTNERSHIP),
                entry(SERVE_PAPERS_BEFORE_DATE, "16 July 2021"),
                entry(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP),
                entry(BEEN_MARRIED_OR_ENTERED_INTO_CIVIL_PARTNERSHIP, ENTERED_INTO_A_CIVIL_PARTNERSHIP_WITH),
                entry(MARRIAGE_OR_CIVIL_PARTNER, CIVIL_PARTNERSHIP),
                entry("ctscContactDetails", getCtscContactDetails()),
                entry(APPLICANT_1_ADDRESS, "line1\nline2\nUK"),
                entry(APPLICANT_2_ADDRESS, "10 the street\nthe town\nUK"),
                entry(APPLICANT_1_SOLICITOR_NAME, "Not represented"),
                entry(DISPLAY_EMAIL_CONFIRMATION, true),
                entry("applicant2FirstName", APPLICANT_2_FIRST_NAME),
                entry("applicant2LastName", APPLICANT_2_LAST_NAME),
                entry(DIVORCE_OR_END_THEIR_CIVIL_PARTNERSHIP, TO_END_THEIR_CIVIL_PARTNERSHIP),
                entry(RESPOND_BY_DATE, "4 July 2021"),
                entry(IS_COURT_SERVICE, false),
                entry(IS_PERSONAL_SERVICE, false),
                entry(CAN_SERVE_BY_EMAIL, true),
                entry(IS_RESPONDENT_BASED_IN_UK, true),
                entry(IS_RESPONDENT_SOLICITOR_PERSONAL_SERVICE, false),
                entry(IS_DIVORCE, false),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_DOCUMENTS, CIVIL_PARTNERSHIP_DOCUMENTS),
                entry(IS_OFFLINE, false)
            );
    }

    private CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .emailAddress(CONTACT_DIVORCE_EMAIL)
            .phoneNumber("0300 303 0642")
            .build();
    }

    @Test
    void shouldThrowRuntimeExceptionOnCasesNotIssued() {
        CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(null);

        assertThatThrownBy(() -> noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2(), ENGLISH))
            .isInstanceOf(InvalidCcdCaseDataException.class)
            .hasMessage("Cannot generate notice of proceeding without issue date. Case ID: 1616591401473378");
    }

}
