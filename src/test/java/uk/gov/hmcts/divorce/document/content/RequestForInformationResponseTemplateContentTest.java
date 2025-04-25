package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;
import uk.gov.hmcts.divorce.document.content.templatecontent.RequestForInformationResponseTemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID;
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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
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
import static uk.gov.hmcts.divorce.notification.CommonContent.SENT_TO_BOTH_APPLICANTS;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_CY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicSolicitorTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getOfflineRequestForInformationCaseDetails;

@ExtendWith(MockitoExtension.class)
class RequestForInformationResponseTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private RequestForInformationResponseTemplateContent requestForInformationResponseTemplateContent;

    @Test
    void shouldMapTemplateContentForApplicant1() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(APPLICANT1, false, false).getData();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant2() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(APPLICANT2, false, false).getData();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    void shouldMapTemplateContentForWelshApplicant() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(RequestForInformationSoleParties.APPLICANT, false, false).getData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(WELSH)).thenReturn(getBasicDocmosisTemplateContent(WELSH));

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant1WhenRFISentToBothParties() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, false, false).getData();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE);

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE)
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant1WhenRFISentToBothPartiesWelsh() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, false, false).getData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(WELSH)).thenReturn(getBasicDocmosisTemplateContent(WELSH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE_CY);

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE_CY)
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant2WhenRFISentToBothParties() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, false, false).getData();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND);

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND)
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant2WhenRFISentToBothPartiesWelsh() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, false, false).getData();
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(WELSH)).thenReturn(getBasicDocmosisTemplateContent(WELSH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND_CY);

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getFullName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
            entry(CASE_REFERENCE, TEST_CASE_ID),
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND_CY)
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant1Solicitor() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(RequestForInformationSoleParties.APPLICANT, true, false).getData();
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, ENGLISH));

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant1SolicitorWelsh() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(RequestForInformationSoleParties.APPLICANT, true, false).getData();
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, WELSH));

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant2Solicitor() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(APPLICANT2, false, true).getData();

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, ENGLISH));

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant2SolicitorWelsh() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(APPLICANT2, false, true).getData();
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, WELSH));

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, NO),
            entry(PARTNER, "")
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant1SolicitorWhenRFISentToBothParties() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, true, false).getData();
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, ENGLISH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND);


        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND)
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant1SolicitorWhenRFISentToBothPartiesWelsh() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, true, false).getData();
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, true, WELSH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(HUSBAND_CY);

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant1());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant1().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, HUSBAND_CY)
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant2SolicitorWhenRFISentToBothParties() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, false, true).getData();

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, ENGLISH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE);

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE)
        );
    }

    @Test
    void shouldMapTemplateContentForApplicant2SolicitorWhenRFISentToBothPartiesWelsh() {
        CaseData caseData = getOfflineRequestForInformationCaseDetails(BOTH, false, true).getData();
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, WELSH))
            .thenReturn(getBasicSolicitorTemplateContent(caseData, false, WELSH));
        when(commonContent.getPartner(any(), any(), any())).thenReturn(WIFE_CY);

        final Map<String, Object> templateContent = requestForInformationResponseTemplateContent.getTemplateContent(caseData,
            TEST_CASE_ID,
            caseData.getApplicant2());

        assertThat(templateContent).contains(
            entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
            entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
            entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
            entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
            entry(RECIPIENT_NAME, caseData.getApplicant2().getSolicitor().getName()),
            entry(RECIPIENT_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck()),
            entry(DATE, LocalDate.now()),
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
            entry(SENT_TO_BOTH_APPLICANTS, CommonContent.YES),
            entry(PARTNER, WIFE_CY)
        );
    }

    @Test
    void shouldGetSupportedTemplates() {
        assertThat(requestForInformationResponseTemplateContent.getSupportedTemplates()).contains(
            REQUEST_FOR_INFORMATION_RESPONSE_LETTER_TEMPLATE_ID,
            REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID
        );
    }
}
