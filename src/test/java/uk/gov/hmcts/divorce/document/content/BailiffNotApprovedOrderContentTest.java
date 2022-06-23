package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.REFUSAL_REASON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.jointCaseDataWithOrderSummary;

@ExtendWith(MockitoExtension.class)
public class BailiffNotApprovedOrderContentTest {

    @Mock
    private Clock clock;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private BailiffNotApprovedOrderContent templateContent;

    private static final String FULL_NAME = String.format("%s %s %s", TEST_FIRST_NAME, TEST_MIDDLE_NAME, TEST_LAST_NAME);
    private static final LocalDate SERVICE_APPLICATION_DATE = LocalDate.of(2021, 12, 25);

    @Test
    public void shouldSuccessfullyApplyDivorceContent() {
        setMockClock(clock);

        final CaseData caseData = jointCaseDataWithOrderSummary();
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reason");
        caseData.getAlternativeService().setReceivedServiceApplicationDate(SERVICE_APPLICATION_DATE);

        Mockito.when(commonContent.getPartner(caseData, caseData.getApplicant2(), LanguagePreference.ENGLISH)).thenReturn("wife");

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(PETITIONER_FULL_NAME, FULL_NAME),
            entry(RESPONDENT_FULL_NAME, FULL_NAME),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, SERVICE_APPLICATION_DATE.format(DATE_TIME_FORMATTER)),
            entry(REFUSAL_REASON, "refusal reason"),
            entry(THE_APPLICATION, DIVORCE_APPLICATION),
            entry(PARTNER, "wife")
        );
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionContent() {
        setMockClock(clock);

        final CaseData caseData = jointCaseDataWithOrderSummary();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reason");
        caseData.getAlternativeService().setReceivedServiceApplicationDate(SERVICE_APPLICATION_DATE);
        Mockito.when(commonContent.getPartner(caseData, caseData.getApplicant2(), LanguagePreference.ENGLISH)).thenReturn("civil partner");

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(CCD_CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(PETITIONER_FULL_NAME, FULL_NAME),
            entry(RESPONDENT_FULL_NAME, FULL_NAME),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, SERVICE_APPLICATION_DATE.format(DATE_TIME_FORMATTER)),
            entry(REFUSAL_REASON, "refusal reason"),
            entry(THE_APPLICATION, APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP),
            entry(PARTNER, "civil partner")
        );
    }

    @Test
    public void shouldSuccessfullyApplyDivorceWelshContent() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.YES)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .alternativeService(AlternativeService.builder().receivedServiceApplicationDate(SERVICE_APPLICATION_DATE).build())
            .build();

        Mockito.when(commonContent.getPartner(caseData, caseData.getApplicant2(), LanguagePreference.WELSH)).thenReturn("gŵr");

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(THE_APPLICATION, DIVORCE_APPLICATION_CY),
            entry(PARTNER, "gŵr")
        );
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionWelshContent() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.YES)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .alternativeService(AlternativeService.builder().receivedServiceApplicationDate(SERVICE_APPLICATION_DATE).build())
            .build();

        Mockito.when(commonContent.getPartner(caseData, caseData.getApplicant2(), LanguagePreference.WELSH)).thenReturn("gŵr");

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(THE_APPLICATION, END_CIVIL_PARTNERSHIP_CY),
            entry(PARTNER, "gŵr")
        );
    }
}
