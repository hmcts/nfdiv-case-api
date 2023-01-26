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
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
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
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
class DocmosisCommonContentTest {

    private static final String PO_BOX = "PO Box 13226";
    private static final String TOWN = "Harlow";
    private static final String POSTCODE = "CM20 9UG";
    private static final String PHONE_NUMBER = "0300 303 0642";

    private static final int EXPECTED_ENTRY_SIZE = 5;
    private static final int EXPECTED_SOLICITOR_ENTRY_SIZE = 17;

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
        .builder()
        .poBox(PO_BOX)
        .town(TOWN)
        .postcode(POSTCODE)
        .phoneNumber(PHONE_NUMBER)
        .build();

    @InjectMocks
    private DocmosisCommonContent docmosisCommonContent;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(docmosisCommonContent, "poBox", PO_BOX);
        ReflectionTestUtils.setField(docmosisCommonContent, "town", TOWN);
        ReflectionTestUtils.setField(docmosisCommonContent, "postcode", POSTCODE);
        ReflectionTestUtils.setField(docmosisCommonContent, "phoneNumber", PHONE_NUMBER);
    }

    @Test
    void shouldReturnBasicEnglishTemplateContentForEnglish() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(NO).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT));
    }

    @Test
    void shouldReturnBasicWelshTemplateContentForWelsh() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(YES).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT));
    }

    @Test
    void shouldReturnBasicSolicitorEnglishTemplateContentForSoleDivorceCase() {

        Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .address(TEST_SOLICITOR_ADDRESS)
            .build();

        Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .solicitor(solicitor)
            .solicitorRepresented(YES)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .applicant2(applicant)
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        var templateContent = docmosisCommonContent.getBasicSolicitorTemplateContent(
            caseData,
            TEST_CASE_ID,
            true,
            ENGLISH);

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_SOLICITOR_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(APPLICANT_1_FIRST_NAME, applicant.getFirstName()),
                entry(APPLICANT_1_LAST_NAME, applicant.getLastName()),
                entry(APPLICANT_2_FIRST_NAME, applicant.getFirstName()),
                entry(APPLICANT_2_LAST_NAME, applicant.getLastName()),
                entry(IS_JOINT, false),
                entry(IS_DIVORCE, true),
                entry(APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(APPLICANT_2_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS));
    }

    @Test
    void shouldReturnBasicSolicitorEnglishTemplateContentForJointDissolutionCase() {

        Solicitor solicitor = Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .address(TEST_SOLICITOR_ADDRESS)
            .build();

        Applicant applicant = Applicant.builder()
            .languagePreferenceWelsh(NO)
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .solicitor(solicitor)
            .solicitorRepresented(YES)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .applicant2(applicant)
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .build();

        var templateContent = docmosisCommonContent.getBasicSolicitorTemplateContent(
            caseData,
            TEST_CASE_ID,
            false,
            ENGLISH);

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_SOLICITOR_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(APPLICANT_1_FIRST_NAME, applicant.getFirstName()),
                entry(APPLICANT_1_LAST_NAME, applicant.getLastName()),
                entry(APPLICANT_2_FIRST_NAME, applicant.getFirstName()),
                entry(APPLICANT_2_LAST_NAME, applicant.getLastName()),
                entry(IS_JOINT, true),
                entry(IS_DIVORCE, false),
                entry(APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(APPLICANT_2_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                entry(SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS));
    }
}
