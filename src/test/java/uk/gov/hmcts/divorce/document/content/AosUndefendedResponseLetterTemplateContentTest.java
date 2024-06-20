package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.content.templatecontent.AosUndefendedResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;

@ExtendWith(MockitoExtension.class)
public class AosUndefendedResponseLetterTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private AosUndefendedResponseLetterTemplateContent templateContent;

    private static final LocalDate ISSUE_DATE = LocalDate.of(2022, 2, 2);
    private static final LocalDate DATE_TO_WAIT_UNTIL_APPLY_FOR_CO = LocalDate.of(2022, 6, 23);

    @BeforeEach
    public void setup() {
        when(holdingPeriodService.getDueDateFor(ISSUE_DATE)).thenReturn(DATE_TO_WAIT_UNTIL_APPLY_FOR_CO);
        when(docmosisCommonContent.getBasicDocmosisTemplateContent(any())).thenReturn(getBasicContent());
    }

    @Test
    public void shouldSuccessfullyApplyDivorceContent() {

        CaseData caseData = buildCaseData(DIVORCE);

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry("basicKey", "basicValue"),
            entry("caseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "line 1\ntown\nUK\npostcode"),
            entry("relation", "wife"),
            entry("isDivorce", true),
            entry("issueDate", "2 February 2022"),
            entry("dateToWaitUntilApplyForCO", "23 June 2022"),
            entry("divorceOrCivilPartnershipEmail", "contactdivorce@justice.gov.uk"),
            entry("divorceOrCivilPartnershipServiceHeader", "The Divorce Service")
        );
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionContent() {
        CaseData caseData = buildCaseData(DISSOLUTION);

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("civil partner");

        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry("basicKey", "basicValue"),
            entry("caseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "line 1\ntown\nUK\npostcode"),
            entry("relation", "civil partner"),
            entry("isDivorce", false),
            entry("issueDate", "2 February 2022"),
            entry("dateToWaitUntilApplyForCO", "23 June 2022"),
            entry("divorceOrCivilPartnershipEmail", "civilpartnership.case@justice.gov.uk"),
            entry("divorceOrCivilPartnershipServiceHeader", "End A Civil Partnership Service")
        );
    }

    @Test
    public void shouldSuccessfullyApplyDivorceContentWithJSApp1NoSol() {

        CaseData caseData = buildCaseDataWithJSAppSol(DIVORCE, false, false);

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result.size()).isEqualTo(18);
        assertThat(result).contains(
            entry("basicKey", "basicValue"),
            entry("caseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "line 1\ntown\nUK\npostcode"),
            entry("relation", "wife"),
            entry("isDivorce", true),
            entry("issueDate", "2 February 2022"),
            entry("dateToWaitUntilApplyForCO", "23 June 2022"),
            entry("divorceOrCivilPartnershipEmail", "contactdivorce@justice.gov.uk"),
            entry("divorceOrCivilPartnershipServiceHeader", "The Divorce Service"),
            entry("recipientName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME),
            entry("recipientAddress", "line 1\ntown\nUK\npostcode"),
            entry("applicant2FirstName", TEST_FIRST_NAME),
            entry("applicant2LastName", TEST_LAST_NAME),
            entry("solicitorName", "Not represented"),
            entry("applicant2SolicitorName", "Not represented"),
            entry("solicitorReference", "Not represented")
        );
    }

    @Test
    public void shouldSuccessfullyApplyDivorceContentWithJSApp1Sol() {

        CaseData caseData = buildCaseDataWithJSAppSol(DIVORCE, true, false);

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result.size()).isEqualTo(18);
        assertThat(result).contains(
            entry("basicKey", "basicValue"),
            entry("caseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "App1SolAddress"),
            entry("relation", "wife"),
            entry("isDivorce", true),
            entry("issueDate", "2 February 2022"),
            entry("dateToWaitUntilApplyForCO", "23 June 2022"),
            entry("divorceOrCivilPartnershipEmail", "contactdivorce@justice.gov.uk"),
            entry("divorceOrCivilPartnershipServiceHeader", "The Divorce Service"),
            entry("recipientName", "App1SolName"),
            entry("recipientAddress", "App1SolAddress"),
            entry("applicant2FirstName", TEST_FIRST_NAME),
            entry("applicant2LastName", TEST_LAST_NAME),
            entry("solicitorName", "App1SolName"),
            entry("applicant2SolicitorName", "Not represented"),
            entry("solicitorReference", "App1SolRef")
        );
    }

    @Test
    public void shouldSuccessfullyApplyDivorceContentWithJSApp1And2Sol() {

        CaseData caseData = buildCaseDataWithJSAppSol(DIVORCE, true, true);

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result.size()).isEqualTo(18);
        assertThat(result).contains(
            entry("basicKey", "basicValue"),
            entry("caseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "App1SolAddress"),
            entry("relation", "wife"),
            entry("isDivorce", true),
            entry("issueDate", "2 February 2022"),
            entry("dateToWaitUntilApplyForCO", "23 June 2022"),
            entry("divorceOrCivilPartnershipEmail", "contactdivorce@justice.gov.uk"),
            entry("divorceOrCivilPartnershipServiceHeader", "The Divorce Service"),
            entry("recipientName", "App1SolName"),
            entry("recipientAddress", "App1SolAddress"),
            entry("applicant2FirstName", TEST_FIRST_NAME),
            entry("applicant2LastName", TEST_LAST_NAME),
            entry("solicitorName", "App1SolName"),
            entry("applicant2SolicitorName", "App2SolName"),
            entry("solicitorReference", "App1SolRef")
        );
    }

    private CaseData buildCaseData(final DivorceOrDissolution divorceOrDissolution) {
        final Applicant applicant1 = TestDataHelper.getApplicantWithAddress();

        return CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(divorceOrDissolution)
            .applicant1(applicant1)
            .application(
                Application.builder().issueDate(ISSUE_DATE).build()
            )
            .build();
    }

    private CaseData buildCaseDataWithJSAppSol(final DivorceOrDissolution divorceOrDissolution, boolean app1Represented,
                                               boolean app2Represented) {
        final Applicant applicant1 = TestDataHelper.getApplicantWithAddress();
        if (app1Represented) {
            applicant1.setSolicitor(Solicitor.builder()
                .name("App1SolName")
                .address("App1SolAddress")
                .reference("App1SolRef")
                .build());
            applicant1.setSolicitorRepresented(YES);
        }

        final Applicant applicant2 = TestDataHelper.getApplicantWithAddress();
        if (app2Represented) {
            applicant2.setSolicitor(Solicitor.builder()
                .name("App2SolName")
                .address("App2SolAddress")
                .reference("App2SolRef")
                .build());
            applicant2.setSolicitorRepresented(YES);
        }

        return CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(divorceOrDissolution)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .application(
                Application.builder().issueDate(ISSUE_DATE).build()
            )
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();
    }

    private Map<String, Object> getBasicContent() {
        Map<String, Object> basic = new HashMap<>();
        basic.put("basicKey", "basicValue");
        return basic;
    }

}
