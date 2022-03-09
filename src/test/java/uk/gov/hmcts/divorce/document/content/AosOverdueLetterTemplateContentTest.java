package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
public class AosOverdueLetterTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private AosOverdueLetterTemplateContent templateContent;

    private static final LocalDate ISSUE_DATE = LocalDate.of(2022, 2, 2);

    @BeforeEach
    public void setup() {
        when(paymentService.getServiceCost(anyString(), anyString(), anyString())).thenReturn(45.0);
    }

    @Test
    public void shouldSuccessfullyApplyDivorceContent() {

        CaseData caseData = buildCaseData(DIVORCE);

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry("caseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "line 1\ntown\npostcode"),
            entry("relation", "wife"),
            entry("isDivorce", true),
            entry("issueDate", "2 February 2022"),
            entry("arrangeServiceBeforeDate", "2 March 2022"),
            entry("searchAddressCost", "£45.00"),
            entry("serveTheApplicationByEmailCost", "£45.00"),
            entry("bailiffServiceCost", "£45.00"),
            entry("deemedServiceCost", "£45.00"),
            entry("dispensedServiceCost", "£45.00"),
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
            entry("caseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "line 1\ntown\npostcode"),
            entry("relation", "civil partner"),
            entry("isDivorce", false),
            entry("issueDate", "2 February 2022"),
            entry("arrangeServiceBeforeDate", "2 March 2022"),
            entry("searchAddressCost", "£45.00"),
            entry("serveTheApplicationByEmailCost", "£45.00"),
            entry("bailiffServiceCost", "£45.00"),
            entry("deemedServiceCost", "£45.00"),
            entry("dispensedServiceCost", "£45.00"),
            entry("divorceOrCivilPartnershipEmail", "civilpartnership.case@justice.gov.uk"),
            entry("divorceOrCivilPartnershipServiceHeader", "End A Civil Partnership Service")
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
}
