package uk.gov.hmcts.divorce.document.content.templatecontent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;

@ExtendWith(MockitoExtension.class)
class JSSwitchToSoleCoSolicitorTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private JSSwitchToSoleCoSolicitorTemplateContent jsSwitchToSoleCoSolicitorTemplateContent;

    @Test
    void shouldApplyContentFromCaseDataForJSSwitchToSoleSolicitorWhenApp2isRepresentedAndNoSolicitorReference() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .solicitorRepresented(YesOrNo.NO)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .gender(Gender.FEMALE)
            .languagePreferenceWelsh(YesOrNo.NO)
            .solicitorRepresented(YES)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));
        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("husband");

        final Map<String, Object> result = jsSwitchToSoleCoSolicitorTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            entry(RESPONDENT_SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(RESPONDENT_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry(SOLICITOR_REFERENCE, NOT_PROVIDED),
            entry(APPLICANT_1_SOLICITOR_NAME, NOT_REPRESENTED),
            entry(RELATION, "husband")
        );
    }

    @Test
    void shouldApplyContentFromCaseDataForJSSwitchToSoleSolicitorWhenBothApplicantsAreRepresented() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .solicitorRepresented(YES)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .reference(TEST_REFERENCE)
                    .build()
            )
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .gender(Gender.FEMALE)
            .languagePreferenceWelsh(YesOrNo.NO)
            .solicitorRepresented(YES)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .reference(TEST_REFERENCE)
                    .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));
        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("wife");

        final Map<String, Object> result = jsSwitchToSoleCoSolicitorTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            entry(RESPONDENT_SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(RESPONDENT_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry(SOLICITOR_REFERENCE, TEST_REFERENCE),
            entry(APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            entry(RELATION, "wife")
        );
    }

    @Test
    void shouldApplyCivilContentFromCaseDataForJSSwitchToSoleSolicitorWhenBothApplicantsAreRepresented() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.NO)
            .solicitorRepresented(YES)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .reference(TEST_REFERENCE)
                    .build()
            )
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(APPLICANT_2_LAST_NAME)
            .gender(Gender.FEMALE)
            .languagePreferenceWelsh(YesOrNo.NO)
            .solicitorRepresented(YES)
            .solicitor(
                Solicitor.builder()
                    .name(TEST_SOLICITOR_NAME)
                    .address(TEST_SOLICITOR_ADDRESS)
                    .reference(TEST_REFERENCE)
                    .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
            .thenReturn(new HashMap<>(Map.of(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT)));
        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("wife");

        final Map<String, Object> result = jsSwitchToSoleCoSolicitorTemplateContent.getTemplateContent(caseData, TEST_CASE_ID, null);

        assertThat(result).contains(
            entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(RESPONDENT_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            entry(RESPONDENT_SOLICITOR_ADDRESS, TEST_SOLICITOR_ADDRESS),
            entry(APPLICANT_1_FULL_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME),
            entry(RESPONDENT_FULL_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "civil partnership"),
            entry(SOLICITOR_REFERENCE, TEST_REFERENCE),
            entry(APPLICANT_1_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
            entry(RELATION, "wife")
        );
    }
}
