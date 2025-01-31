package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.templatecontent.RequestForInformationTemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_OR_APPLICANT1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_OR_APPLICANT2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.REQUEST_FOR_INFORMATION_DETAILS;
import static uk.gov.hmcts.divorce.notification.CommonContent.SENT_TO_BOTH_APPLICANTS;
import static uk.gov.hmcts.divorce.notification.CommonContent.WEBFORM_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_CY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.WEBFORM_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.WEBFORM_TEST_URL_CY;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2WithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicSolicitorTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getOfflineSolicitor;

@ExtendWith(MockitoExtension.class)
public class RequestForInformationTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private RequestForInformationTemplateContent requestForInformationTemplateContent;

    @Test
    public void shouldMapTemplateContentForApplicant1() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant2() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicantWithAddress());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    public void shouldMapTemplateContentForWelshApplicant() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(WELSH)).thenReturn(getBasicDocmosisTemplateContent(WELSH));
        when(commonContent.getWebFormUrl(WELSH)).thenReturn(WEBFORM_TEST_URL_CY);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(WEBFORM_URL, WEBFORM_TEST_URL_CY),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant1WhenRFISentToBothParties() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.setApplicant2(getApplicant2WithAddress());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE)
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant1WhenRFISentToBothPartiesWelsh() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.setApplicant2(getApplicant2WithAddress());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(WELSH)).thenReturn(getBasicDocmosisTemplateContent(WELSH));
        when(commonContent.getWebFormUrl(WELSH)).thenReturn(WEBFORM_TEST_URL_CY);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE_CY);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(WEBFORM_URL, WEBFORM_TEST_URL_CY),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE_CY)
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant2WhenRFISentToBothParties() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.setApplicant2(getApplicant2WithAddress());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND)
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant2WhenRFISentToBothPartiesWelsh() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.setApplicant2(getApplicant2WithAddress());
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(WELSH)).thenReturn(getBasicDocmosisTemplateContent(WELSH));
        when(commonContent.getWebFormUrl(WELSH)).thenReturn(WEBFORM_TEST_URL_CY);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND_CY);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(WEBFORM_URL, WEBFORM_TEST_URL_CY),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND_CY)
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant1Solicitor() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT),
            entry(RESPONDENT_OR_APPLICANT2, RESPONDENT),
            entry(IS_JOINT, false),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(APPLICANT_2_SOLICITOR_NAME, NOT_REPRESENTED),
            entry(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, TEST_REFERENCE),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant1SolicitorWelsh() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, WELSH));
        when(commonContent.getWebFormUrl(WELSH)).thenReturn(WEBFORM_TEST_URL_CY);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT_CY),
            entry(RESPONDENT_OR_APPLICANT2, RESPONDENT_CY),
            entry(IS_JOINT, false),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(APPLICANT_2_SOLICITOR_NAME, NOT_REPRESENTED_CY),
            entry(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, TEST_REFERENCE),
            entry(WEBFORM_URL, WEBFORM_TEST_URL_CY),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant2Solicitor() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(getOfflineSolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT_1),
            entry(RESPONDENT_OR_APPLICANT2, APPLICANT_2),
            entry(IS_JOINT, true),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, NOT_REPRESENTED),
            entry(APPLICANT_2_SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, NOT_PROVIDED),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant2SolicitorWelsh() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, WELSH));
        when(commonContent.getWebFormUrl(WELSH)).thenReturn(WEBFORM_TEST_URL_CY);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT_1_CY),
            entry(RESPONDENT_OR_APPLICANT2, APPLICANT_2_CY),
            entry(IS_JOINT, true),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, NOT_REPRESENTED_CY),
            entry(APPLICANT_2_SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, NOT_PROVIDED_CY),
            entry(WEBFORM_URL, WEBFORM_TEST_URL_CY),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant1SolicitorWhenRFISentToBothParties() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND);


        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT_1),
            entry(RESPONDENT_OR_APPLICANT2, APPLICANT_2),
            entry(IS_JOINT, true),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(APPLICANT_2_SOLICITOR_NAME, NOT_REPRESENTED),
            entry(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, TEST_REFERENCE),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND)
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant1SolicitorWhenRFISentToBothPartiesWelsh() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, WELSH));
        when(commonContent.getWebFormUrl(WELSH)).thenReturn(WEBFORM_TEST_URL_CY);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND_CY);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT_1_CY),
            entry(RESPONDENT_OR_APPLICANT2, APPLICANT_2_CY),
            entry(IS_JOINT, true),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(APPLICANT_2_SOLICITOR_NAME, NOT_REPRESENTED_CY),
            entry(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, TEST_REFERENCE),
            entry(WEBFORM_URL, WEBFORM_TEST_URL_CY),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND_CY)
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant2SolicitorWhenRFISentToBothParties() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(getOfflineSolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, ENGLISH));
        when(commonContent.getWebFormUrl(ENGLISH)).thenReturn(WEBFORM_TEST_URL);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT_1),
            entry(RESPONDENT_OR_APPLICANT2, APPLICANT_2),
            entry(IS_JOINT, true),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, NOT_REPRESENTED),
            entry(APPLICANT_2_SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, NOT_PROVIDED),
            entry(WEBFORM_URL, WEBFORM_TEST_URL),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE)
        );
    }

    @Test
    public void shouldMapTemplateContentForApplicant2SolicitorWhenRFISentToBothPartiesWelsh() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, WELSH));
        when(commonContent.getWebFormUrl(WELSH)).thenReturn(WEBFORM_TEST_URL_CY);
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE_CY);

        final Map<String, Object> templateContent = requestForInformationTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(ISSUE_DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName()),
            entry(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName()),
            entry(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName()),
            entry(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName()),
            entry(APPLICANT_OR_APPLICANT1, APPLICANT_1_CY),
            entry(RESPONDENT_OR_APPLICANT2, APPLICANT_2_CY),
            entry(IS_JOINT, true),
            entry(IS_DIVORCE, true),
            entry(APPLICANT_1_SOLICITOR_NAME, NOT_REPRESENTED_CY),
            entry(APPLICANT_2_SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(SOLICITOR_REFERENCE, NOT_PROVIDED_CY),
            entry(WEBFORM_URL, WEBFORM_TEST_URL_CY),
            entry(REQUEST_FOR_INFORMATION_DETAILS, TEST_TEXT),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE_CY)
        );
    }

    @Test
    public void shouldGetSupportedTemplates() {
        assertThat(requestForInformationTemplateContent.getSupportedTemplates()).contains(
            REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID,
            REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID
        );
    }
}
