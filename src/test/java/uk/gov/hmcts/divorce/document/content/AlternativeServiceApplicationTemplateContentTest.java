package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceDifferentWays;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class AlternativeServiceApplicationTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private AlternativeServiceApplicationTemplateContent templateContent;

    private static final String ALTERNATIVE_SERVICE_REASON = "Test reason for alternative service";
    private static final String ALTERNATIVE_SERVICE_METHOD_JUSTIFICATION = "Test justification for alternative service method";

    @Test
    void shouldReturnTemplateContentForEnglish() {
        final CaseData caseData = buildTestData(AlternativeServiceMethod.EMAIL, null);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        when(docmosisCommonContent.getApplicationType(LanguagePreference.ENGLISH, caseData))
            .thenReturn(DIVORCE_APPLICATION);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", TEST_FIRST_NAME);
        expectedEntries.put("altServiceEvidenceUploaded", true);
        expectedEntries.put("divorceOrDissolution", "divorce application");
        expectedEntries.put("altServiceReasonForApplying", ALTERNATIVE_SERVICE_REASON);
        expectedEntries.put("altServiceEvidenceDetails", ALTERNATIVE_SERVICE_METHOD_JUSTIFICATION);
        expectedEntries.put("byEmail", "true");
        expectedEntries.put("emailAndDifferent", "false");
        expectedEntries.put("byTextMessage", "false");
        expectedEntries.put("byWhatsApp", "false");
        expectedEntries.put("bySocialMedia", "false");
        expectedEntries.put("byOther", "false");
        expectedEntries.put("altServicePartnerEmail", TEST_USER_EMAIL);
        expectedEntries.put("altServicePartnerPhone", null);
        expectedEntries.put("altServicePartnerWANum", null);
        expectedEntries.put("altServicePartnerSocialDetails", null);
        expectedEntries.put("altServicePartnerOtherDetails", null);
        expectedEntries.put("statementOfTruth", "Yes");
        expectedEntries.put("serviceApplicationReceivedDate", "1 January 2023");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldReturnTemplateContentForWelsh() {
        final CaseData caseData = buildTestData(AlternativeServiceMethod.EMAIL, null);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        when(docmosisCommonContent.getApplicationType(LanguagePreference.WELSH, caseData))
            .thenReturn(DIVORCE_APPLICATION_CY);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", TEST_FIRST_NAME);
        expectedEntries.put("altServiceEvidenceUploaded", true);
        expectedEntries.put("divorceOrDissolution", "cais am ysgariad");
        expectedEntries.put("altServiceReasonForApplying", ALTERNATIVE_SERVICE_REASON);
        expectedEntries.put("altServiceEvidenceDetails", ALTERNATIVE_SERVICE_METHOD_JUSTIFICATION);
        expectedEntries.put("byEmail", "true");
        expectedEntries.put("emailAndDifferent", "false");
        expectedEntries.put("byTextMessage", "false");
        expectedEntries.put("byWhatsApp", "false");
        expectedEntries.put("bySocialMedia", "false");
        expectedEntries.put("byOther", "false");
        expectedEntries.put("altServicePartnerEmail", TEST_USER_EMAIL);
        expectedEntries.put("altServicePartnerPhone", null);
        expectedEntries.put("altServicePartnerWANum", null);
        expectedEntries.put("altServicePartnerSocialDetails", null);
        expectedEntries.put("altServicePartnerOtherDetails", null);
        expectedEntries.put("statementOfTruth", "Ydw");
        expectedEntries.put("serviceApplicationReceivedDate", "1 Ionawr 2023");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldReturnTemplateContentWhenEmailAndDifferentMethodSelected() {
        final CaseData caseData = buildTestData(AlternativeServiceMethod.EMAIL_AND_DIFFERENT,
            Set.of(AlternativeServiceDifferentWays.TEXT_MESSAGE));
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        when(docmosisCommonContent.getApplicationType(LanguagePreference.ENGLISH, caseData))
            .thenReturn(DIVORCE_APPLICATION);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", TEST_FIRST_NAME);
        expectedEntries.put("altServiceEvidenceUploaded", true);
        expectedEntries.put("divorceOrDissolution", "divorce application");
        expectedEntries.put("altServiceReasonForApplying", ALTERNATIVE_SERVICE_REASON);
        expectedEntries.put("altServiceEvidenceDetails", ALTERNATIVE_SERVICE_METHOD_JUSTIFICATION);
        expectedEntries.put("byEmail", "false");
        expectedEntries.put("emailAndDifferent", "true");
        expectedEntries.put("byTextMessage", "true");
        expectedEntries.put("byWhatsApp", "false");
        expectedEntries.put("bySocialMedia", "false");
        expectedEntries.put("byOther", "false");
        expectedEntries.put("altServicePartnerEmail", TEST_USER_EMAIL);
        expectedEntries.put("altServicePartnerPhone", "0123456789");
        expectedEntries.put("altServicePartnerWANum", null);
        expectedEntries.put("altServicePartnerSocialDetails", null);
        expectedEntries.put("altServicePartnerOtherDetails", null);
        expectedEntries.put("statementOfTruth", "Yes");
        expectedEntries.put("serviceApplicationReceivedDate", "1 January 2023");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    private CaseData buildTestData(AlternativeServiceMethod alternativeServiceMethod,
                                   Set<AlternativeServiceDifferentWays> alternativeServiceDifferentWays) {
        final String phoneNumber = alternativeServiceDifferentWays != null
            && alternativeServiceDifferentWays.contains(AlternativeServiceDifferentWays.TEXT_MESSAGE) ? "0123456789" : null;
        final String waNum = alternativeServiceDifferentWays != null
            && alternativeServiceDifferentWays.contains(AlternativeServiceDifferentWays.WHATSAPP) ? "0123456789" : null;
        final String socialDetails = alternativeServiceDifferentWays != null
            && alternativeServiceDifferentWays.contains(AlternativeServiceDifferentWays.SOCIAL_MEDIA) ? "socialMediaDetails" : null;
        final String otherDetails = alternativeServiceDifferentWays != null
            && alternativeServiceDifferentWays.contains(AlternativeServiceDifferentWays.OTHER) ? "otherDetails" : null;
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        caseData.getApplicant1().setInterimApplicationOptions(
            InterimApplicationOptions.builder()
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .alternativeServiceJourneyOptions(
                    AlternativeServiceJourneyOptions.builder()
                        .altServiceReasonForApplying(ALTERNATIVE_SERVICE_REASON)
                        .altServiceMethod(alternativeServiceMethod)
                        .altServicePartnerEmail(TEST_USER_EMAIL)
                        .altServicePartnerPhone(phoneNumber)
                        .altServicePartnerWANum(waNum)
                        .altServicePartnerSocialDetails(socialDetails)
                        .altServicePartnerOtherDetails(otherDetails)
                        .altServiceDifferentWays(alternativeServiceDifferentWays)
                        .altServiceMethodJustification(ALTERNATIVE_SERVICE_METHOD_JUSTIFICATION)
                        .build()
                )
                .build()
        );
        caseData.setAlternativeService(
            AlternativeService.builder()
                .receivedServiceApplicationDate(LocalDate.of(2023, 1, 1))
                .build()
        );

        return caseData;
    }
}
