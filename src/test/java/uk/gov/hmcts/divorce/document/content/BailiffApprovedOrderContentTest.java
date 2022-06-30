package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ENDING_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ENDING_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_DIVORCE_CY;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
public class BailiffApprovedOrderContentTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private BailiffApprovedOrderContent templateContent;

    @Test
    public void shouldSuccessfullyApplyDivorceContent() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(THE_APPLICATION, DIVORCE_APPLICATION),
            entry(DIVORCE_OR_DISSOLUTION, THE_DIVORCE),
            entry(CCD_CASE_REFERENCE, TEST_CASE_ID),
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_2_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_2_LAST_NAME, TEST_LAST_NAME)
        );
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionContent() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(THE_APPLICATION, END_CIVIL_PARTNERSHIP),
            entry(DIVORCE_OR_DISSOLUTION, ENDING_CIVIL_PARTNERSHIP),
            entry(CCD_CASE_REFERENCE, TEST_CASE_ID),
            entry(DATE, LocalDate.now().format(DATE_TIME_FORMATTER)),
            entry(APPLICANT_1_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_1_LAST_NAME, TEST_LAST_NAME),
            entry(APPLICANT_2_FIRST_NAME, TEST_FIRST_NAME),
            entry(APPLICANT_2_LAST_NAME, TEST_LAST_NAME)
        );
    }

    @Test
    public void shouldSuccessfullyApplyDivorceWelshContent() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YES)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(THE_APPLICATION, DIVORCE_APPLICATION_CY),
            entry(DIVORCE_OR_DISSOLUTION, THE_DIVORCE_CY)
        );
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionWelshContent() {
        setMockClock(clock);

        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YES)
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .build();

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry(THE_APPLICATION, END_CIVIL_PARTNERSHIP_CY),
            entry(DIVORCE_OR_DISSOLUTION, ENDING_CIVIL_PARTNERSHIP_CY)
        );
    }
}
