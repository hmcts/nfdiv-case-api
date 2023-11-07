package uk.gov.hmcts.divorce.legaladvisor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderCommonContent;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.content.templatecontent.CoRefusalTemplateContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent.LEGAL_ADVISOR_COMMENTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CP_CASE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
public class GenerateCoRefusedCoverLetterTest {

    @Mock
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Mock
    private Clock clock;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private CoRefusalTemplateContent coRefusalTemplateContent;

    @Test
    void shouldGenerateCoRefusedCoverLetterWithDivorceContent() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(FIRST_NAME, "Bob");
        templateContent.put(LAST_NAME, "Smith");
        templateContent.put(ADDRESS, "line1\nline2\ncity\npostcode");
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, false);
        templateContent.put(LEGAL_ADVISOR_COMMENTS, refusalReasons);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()
            )
            .build();

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = coRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }

    @Test
    void shouldGenerateCoRefusedCoverLetterWithDissolutionContent() {
        setMockClock(clock);

        final List<ConditionalOrderCommonContent.RefusalReason> refusalReasons =
            List.of(new ConditionalOrderCommonContent.RefusalReason("Court does not have jurisdiction"));

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(FIRST_NAME, "Bob");
        templateContent.put(LAST_NAME, "Smith");
        templateContent.put(ADDRESS, "line1\nline2\ncity\npostcode");
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, true);
        templateContent.put(LEGAL_ADVISOR_COMMENTS, refusalReasons);
        templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CP_CASE_EMAIL);
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);

        CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .applicationType(JOINT_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .address(APPLICANT_ADDRESS)
                    .languagePreferenceWelsh(NO)
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .gender(FEMALE)
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .refusalRejectionAdditionalInfo("Court does not have jurisdiction")
                    .build()
            )
            .build();

        when(conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()))
            .thenReturn(refusalReasons);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference()));

        Map<String, Object> actualTemplateContent = coRefusalTemplateContent.templateContent(caseData,
            TEST_CASE_ID, caseData.getApplicant1());

        assertThat(actualTemplateContent.entrySet()).containsAll(templateContent.entrySet());
    }
}
