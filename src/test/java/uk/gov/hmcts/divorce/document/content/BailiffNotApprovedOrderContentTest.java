package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
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
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

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

        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reason");
        caseData.getAlternativeService().setReceivedServiceApplicationDate(SERVICE_APPLICATION_DATE);

        Mockito.when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

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

        CtscContactDetails ctscContactDetails = (CtscContactDetails) result.get(CTSC_CONTACT_DETAILS);
        assertEquals(CONTACT_DIVORCE_JUSTICE_GOV_UK, ctscContactDetails.getEmailAddress());
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionContent() {
        setMockClock(clock);

        final CaseData caseData = validJointApplicant1CaseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.getAlternativeService().setServiceApplicationRefusalReason("refusal reason");
        caseData.getAlternativeService().setReceivedServiceApplicationDate(SERVICE_APPLICATION_DATE);
        Mockito.when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("civil partner");

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

        CtscContactDetails ctscContactDetails = (CtscContactDetails) result.get(CTSC_CONTACT_DETAILS);
        assertEquals(CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK, ctscContactDetails.getEmailAddress());
    }
}
