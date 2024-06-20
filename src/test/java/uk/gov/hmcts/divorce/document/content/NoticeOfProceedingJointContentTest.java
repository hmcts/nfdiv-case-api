package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PROCESS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DISPLAY_EMAIL_CONFIRMATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE;
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
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_URL;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DUE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.ENDING_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.END_A_CIVIL_PARTNERSHIP_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.MARRIAGE_OR_CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.PROCEEDINGS_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.RELATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.THE_DIVORCE_SERVICE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
class NoticeOfProceedingJointContentTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisTemplatesConfig config;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private NoticeOfProceedingJointContent noticeOfProceedingJointContent;

    @Test
    public void shouldSuccessfullyApplyDivorceContentForJointNoticeOfProceedings() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setAddress(
                AddressGlobalUK
                        .builder()
                        .addressLine1("line1")
                        .addressLine2("line2")
                        .country("UK")
                        .build()
        );

        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 19));

        var ctscContactDetails = CtscContactDetails
                .builder()
                .centreName("HMCTS Digital Divorce and Dissolution")
                .serviceCentre("Courts and Tribunals Service Centre")
                .poBox("PO Box 13226")
                .town("Harlow")
                .postcode("CM20 9UG")
                .phoneNumber("0300 303 0642")
                .emailAddress("contactdivorce@justice.gov.uk")
                .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn("wife");
        when(holdingPeriodService.getDueDateFor(LocalDate.of(2021, 6, 18)))
                .thenReturn(LocalDate.of(2021, 11, 6));

        Map<String, Object> templateContent = noticeOfProceedingJointContent.apply(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1(),
                caseData.getApplicant2());

        assertThat(templateContent)
                .contains(
                        entry(CASE_REFERENCE, formatId(1616591401473378L)),
                        entry(FIRST_NAME, TEST_FIRST_NAME),
                        entry(LAST_NAME, TEST_LAST_NAME),
                        entry(ISSUE_DATE, "18 June 2021"),
                        entry(DUE_DATE, "19 June 2021"),
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
                        entry(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, DIVORCE),
                        entry(MARRIAGE_OR_CIVIL_PARTNER, MARRIAGE),
                        entry("ctscContactDetails", ctscContactDetails),
                        entry(ADDRESS, "line1\nline2\nUK"),
                        entry(DISPLAY_EMAIL_CONFIRMATION, true)
            );
    }

    @Test
    public void shouldSuccessfullyApplyCivilPartnershipContentForJointNoticeOfProceedings() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getApplicant2().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant2().setLastName(TEST_LAST_NAME);
        caseData.getApplicant2().setMiddleName(TEST_MIDDLE_NAME);
        caseData.getApplicant1().setOffline(NO);
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
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

        var ctscContactDetails = CtscContactDetails
                .builder()
                .centreName("HMCTS Digital Divorce and Dissolution")
                .serviceCentre("Courts and Tribunals Service Centre")
                .poBox("PO Box 13226")
                .town("Harlow")
                .postcode("CM20 9UG")
                .phoneNumber("0300 303 0642")
                .emailAddress("contactdivorce@justice.gov.uk")
                .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).thenReturn(CIVIL_PARTNER);
        when(holdingPeriodService.getDueDateFor(LocalDate.of(2021, 6, 18)))
                .thenReturn(LocalDate.of(2021, 11, 6));

        Map<String, Object> templateContent = noticeOfProceedingJointContent.apply(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1(),
                caseData.getApplicant2());

        assertThat(templateContent)
                .contains(
                        entry(CASE_REFERENCE, formatId(1616591401473378L)),
                        entry(FIRST_NAME, TEST_FIRST_NAME),
                        entry(LAST_NAME, TEST_LAST_NAME),
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
                        entry(DIVORCE_OR_END_YOUR_CIVIL_PARTNERSHIP, APPLICATION_TO_END_YOUR_CIVIL_PARTNERSHIP),
                        entry(MARRIAGE_OR_CIVIL_PARTNER, CIVIL_PARTNERSHIP),
                        entry("ctscContactDetails", ctscContactDetails),
                        entry(ADDRESS, "line1\nline2\nUK"),
                        entry(DISPLAY_EMAIL_CONFIRMATION, true)
            );
    }
}
