package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.templatecontent.JudicialSeparationCoRefusalTemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
class GenerateJudicialSeparationCORefusedForAmendmentCoverLetterTest {

    @Mock
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private JudicialSeparationCoRefusalTemplateContent judicialSeparationCoRefusalTemplateContent;

    @Test
    void shouldFetchTemplateContentForJudicialSeparationCase() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, true);
        templateContent.put(FIRST_NAME, "Bob");
        templateContent.put(LAST_NAME, "Smith");
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CommonContent.DIVORCE);
        templateContent.put(FEEDBACK, refusalReasons);
        templateContent.put(PARTNER, "wife");
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(Applicant.builder()
                    .gender(FEMALE)
                    .build())
            .conditionalOrder(ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()).build();

        templateContent.put(ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder())).thenReturn(refusalReasons);
        when(conditionalOrderCommonContent.getPartner(caseData)).thenReturn("wife");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = judicialSeparationCoRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }

    @Test
    void shouldGenerateAndUpdateCaseDataForRepresentedApplicant() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, true);
        templateContent.put(FEEDBACK, refusalReasons);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        templateContent.put(SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(SOLICITOR_FIRM, "App1 Solicitor Firm");
        templateContent.put(SOLICITOR_ADDRESS, "App1 Solicitor Address");
        templateContent.put(SOLICITOR_REFERENCE, "App1 SolicitorReference");
        templateContent.put(APPLICANT_1_SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, "App2 Solicitor Name");
        templateContent.put(APPLICANT_1_FULL_NAME, "Bob Smith");
        templateContent.put(APPLICANT_2_FULL_NAME, "Roberta Smith");

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder()
                            .name("App1 Solicitor Name")
                            .firmName("App1 Solicitor Firm")
                            .address("App1 Solicitor Address")
                            .reference("App1 SolicitorReference")
                            .build())
                    .firstName("Bob")
                    .lastName("Smith")
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(
                        Solicitor.builder()
                            .name("App2 Solicitor Name")
                            .firmName("App2 Solicitor Firm")
                            .address("App2 Solicitor Address")
                            .reference("App2 SolicitorReference")
                            .build())
                    .firstName("Roberta")
                    .lastName("Smith")
                    .build()
            )
            .application(Application.builder()
                    .newPaperCase(YES)
                    .build())
            .conditionalOrder(ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()).build();

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = judicialSeparationCoRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }

    @Test
    void shouldGenerateAndUpdateCaseDataForRepresentedApplicantWithNoReferenceAndUnrepresentedRespondent() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CONTACT_EMAIL, "contactdivorce@justice.gov.uk");
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, false);
        templateContent.put(FEEDBACK, refusalReasons);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        templateContent.put(SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(SOLICITOR_FIRM, "App1 Solicitor Firm");
        templateContent.put(SOLICITOR_ADDRESS, "App1 Solicitor Address");
        templateContent.put(SOLICITOR_REFERENCE, "not provided");
        templateContent.put(APPLICANT_1_SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, "not represented");
        templateContent.put(APPLICANT_1_FULL_NAME, "Bob Smith");
        templateContent.put(APPLICANT_2_FULL_NAME, "Roberta Smith");

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder()
                            .name("App1 Solicitor Name")
                            .firmName("App1 Solicitor Firm")
                            .address("App1 Solicitor Address")
                            .build())
                    .firstName("Bob")
                    .lastName("Smith")
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(Applicant.builder()
                    .solicitorRepresented(NO)
                    .firstName("Roberta")
                    .lastName("Smith")
                    .build())
            .application(Application.builder()
                    .newPaperCase(YES)
                    .build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()).build();

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = judicialSeparationCoRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }

    @Test
    void shouldFetchTemplateContentForClarificationJudicialSeparationCase() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, true);
        templateContent.put(FIRST_NAME, "Bob");
        templateContent.put(LAST_NAME, "Smith");
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CommonContent.DIVORCE);
        templateContent.put(FEEDBACK, refusalReasons);
        templateContent.put(PARTNER, "wife");
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(Applicant.builder()
                    .gender(FEMALE)
                    .build())
            .conditionalOrder(ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()).build();

        templateContent.put(ADDRESS, caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck());

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);
        when(conditionalOrderCommonContent.getPartner(caseData))
            .thenReturn("wife");
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = judicialSeparationCoRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }

    @Test
    void shouldGenerateAndUpdateCaseDataForOfflineRepresentedApplicant() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, true);
        templateContent.put(FEEDBACK, refusalReasons);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        templateContent.put(SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(SOLICITOR_FIRM, "App1 Solicitor Firm");
        templateContent.put(SOLICITOR_ADDRESS, "App1 Solicitor Address");
        templateContent.put(SOLICITOR_REFERENCE, "App1 SolicitorReference");
        templateContent.put(APPLICANT_1_SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, "App2 Solicitor Name");
        templateContent.put(APPLICANT_1_FULL_NAME, "Bob Smith");
        templateContent.put(APPLICANT_2_FULL_NAME, "Roberta Smith");

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(JOINT_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder().name("App1 Solicitor Name").firmName("App1 Solicitor Firm")
                        .address("App1 Solicitor Address").reference("App1 SolicitorReference").build())
                    .firstName("Bob")
                    .lastName("Smith")
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(
                        Solicitor.builder().name("App2 Solicitor Name").firmName("App2 Solicitor Firm")
                            .address("App2 Solicitor Address").reference("App2 SolicitorReference").build())
                    .firstName("Roberta")
                    .lastName("Smith")
                    .build())
            .application(Application.builder()
                    .newPaperCase(YES)
                    .build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()).build();

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = judicialSeparationCoRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }

    @Test
    void shouldGenerateAndUpdateCaseDataForOfflineRepresentedApplicantWithNoReferenceAndUnrepresentedRespondent() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, false);
        templateContent.put(FEEDBACK, refusalReasons);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        templateContent.put(SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(SOLICITOR_FIRM, "App1 Solicitor Firm");
        templateContent.put(SOLICITOR_ADDRESS, "App1 Solicitor Address");
        templateContent.put(SOLICITOR_REFERENCE, "not provided");
        templateContent.put(APPLICANT_1_SOLICITOR_NAME, "App1 Solicitor Name");
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, "not represented");
        templateContent.put(APPLICANT_1_FULL_NAME, "Bob Smith");
        templateContent.put(APPLICANT_2_FULL_NAME, "Roberta Smith");

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .applicant1(Applicant.builder()
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder().name("App1 Solicitor Name").firmName("App1 Solicitor Firm")
                            .address("App1 Solicitor Address").build())
                    .firstName("Bob")
                    .lastName("Smith")
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(Applicant.builder()
                    .solicitorRepresented(YesOrNo.NO)
                    .firstName("Roberta").lastName("Smith").build())
            .application(Application.builder().newPaperCase(YES).build())
            .conditionalOrder(ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()).build();

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = judicialSeparationCoRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }
}
