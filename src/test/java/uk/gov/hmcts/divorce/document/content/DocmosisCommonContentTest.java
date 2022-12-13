package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_JUSTICE_GOV_UK_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.TEST_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithSolicitor;

@ExtendWith(MockitoExtension.class)
class DocmosisCommonContentTest {

    private static final String PO_BOX = "PO Box 13226";
    private static final String PO_BOX_CY = "Blwch Post 13226";
    private static final String TOWN = "Harlow";
    private static final String POSTCODE = "CM20 9UG";
    private static final String PHONE_NUMBER = "0300 303 0642";

    private static final int EXPECTED_ENTRY_SIZE = 5;

    private static final CtscContactDetails CONTACT_DETAIL_ENG = CtscContactDetails
        .builder()
        .poBox(PO_BOX)
        .town(TOWN)
        .postcode(POSTCODE)
        .phoneNumber(PHONE_NUMBER)
        .emailAddress(CONTACT_DIVORCE_EMAIL)
        .build();

    private static final CtscContactDetails CONTACT_DETAIL_CY = CtscContactDetails
        .builder()
        .poBox(PO_BOX_CY)
        .town(TOWN)
        .postcode(POSTCODE)
        .phoneNumber(PHONE_NUMBER)
        .emailAddress(CONTACT_JUSTICE_GOV_UK_CY)
        .build();

    @InjectMocks
    private DocmosisCommonContent docmosisCommonContent;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(docmosisCommonContent, "poBox", PO_BOX);
        ReflectionTestUtils.setField(docmosisCommonContent, "poBoxCy", PO_BOX_CY);
        ReflectionTestUtils.setField(docmosisCommonContent, "town", TOWN);
        ReflectionTestUtils.setField(docmosisCommonContent, "postcode", POSTCODE);
        ReflectionTestUtils.setField(docmosisCommonContent, "phoneNumber", PHONE_NUMBER);
    }

    @Test
    void shouldReturnEnglishTemplateContentForEnglish() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(NO).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                entry(CTSC_CONTACT_DETAILS, CONTACT_DETAIL_ENG));
    }

    @Test
    void shouldReturnWelshTemplateContentForWelsh() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(YES).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
                entry(CONTACT_EMAIL, CONTACT_JUSTICE_GOV_UK_CY),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
                entry(CTSC_CONTACT_DETAILS, CONTACT_DETAIL_CY));
    }

    @Test
    public void shouldReturnEnglishSolicitorTemplateContentWhenApplicant1LanguagePreferenceIsEnglish() {
        CaseData caseData = caseDataWithSolicitor();
        caseData.setApplicationType(JOINT_APPLICATION);

        Map<String, Object> templateContent = docmosisCommonContent.getBasicSolicitorTemplateContent(
            caseData, TEST_CASE_ID, true, ENGLISH);

        assertThat(templateContent).isNotEmpty()
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                entry(CTSC_CONTACT_DETAILS, CONTACT_DETAIL_ENG),
                entry(APPLICANT_1_FIRST_NAME, "Bob"),
                entry(APPLICANT_1_LAST_NAME, "Smith"),
                entry(APPLICANT_2_FIRST_NAME, "Julie"),
                entry(APPLICANT_2_LAST_NAME, "Smith"),
                entry(IS_JOINT, true),
                entry(IS_DIVORCE, true),
                entry(APPLICANT_1_SOLICITOR_NAME, "Sol1"),
                entry(APPLICANT_2_SOLICITOR_NAME, "Sol2"),
                entry(SOLICITOR_NAME, "Sol1"),
                entry(SOLICITOR_ADDRESS, TEST_ADDRESS),
                entry(SOLICITOR_REFERENCE, "1234"),
                entry(CASE_REFERENCE, "1616-5914-0147-3378"));
    }

    @Test
    public void shouldReturnEnglishSolicitorTemplateContentWhenApplicant2LanguagePreferenceIsEnglish() {
        CaseData caseData = caseDataWithSolicitor();
        caseData.setApplicationType(JOINT_APPLICATION);

        Map<String, Object> templateContent = docmosisCommonContent.getBasicSolicitorTemplateContent(
            caseData, TEST_CASE_ID, false, ENGLISH);

        assertThat(templateContent).isNotEmpty()
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                entry(CTSC_CONTACT_DETAILS, CONTACT_DETAIL_ENG),
                entry(APPLICANT_1_FIRST_NAME, "Bob"),
                entry(APPLICANT_1_LAST_NAME, "Smith"),
                entry(APPLICANT_2_FIRST_NAME, "Julie"),
                entry(APPLICANT_2_LAST_NAME, "Smith"),
                entry(IS_JOINT, true),
                entry(IS_DIVORCE, true),
                entry(APPLICANT_1_SOLICITOR_NAME, "Sol1"),
                entry(APPLICANT_2_SOLICITOR_NAME, "Sol2"),
                entry(SOLICITOR_NAME, "Sol2"),
                entry(SOLICITOR_ADDRESS, TEST_ADDRESS),
                entry(SOLICITOR_REFERENCE, "4567"),
                entry(CASE_REFERENCE, "1616-5914-0147-3378"));
    }
}
