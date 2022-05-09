package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
public class AosResponseLetterTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private AosResponseLetterTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyDivorceContent() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .offline(YES)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .dueDate(LocalDate.of(2020, 5, 21))
            .application(
                Application.builder().issueDate(LocalDate.of(2020, 1, 1)).build()
            )
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("husband");
        when(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()))
            .thenReturn(caseData.getApplication().getIssueDate().plusDays(141));

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("caseReference", formatId(TEST_CASE_ID));
        expectedEntries.put("applicant1FirstName", TEST_FIRST_NAME);
        expectedEntries.put("applicant1LastName", TEST_LAST_NAME);
        expectedEntries.put("applicant1Address", "Correspondence Address\nLine 2\nLine 3\nPost Town\nPost Code");
        expectedEntries.put("divorceOrCivilPartnershipEmail", "divorcecase@justice.gov.uk");
        expectedEntries.put("divorceOrEndCivilPartnershipApplication", "divorce application");
        expectedEntries.put("issueDate", "1 January 2020");
        expectedEntries.put("relation", "husband");
        expectedEntries.put("waitUntilDate", "21 May 2020");
        expectedEntries.put("divorceOrEndCivilPartnershipProcess", "divorce process");
        expectedEntries.put("divorceOrCivilPartnershipProceedings", "divorce proceedings");
        expectedEntries.put("dueDate", "21 May 2020");
        expectedEntries.put("divorceOrCivilPartnershipServiceHeader", "The Divorce Service");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    public void shouldSuccessfullyApplyDissolutionContent() {
        final Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .offline(YES)
            .address(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .country("UK")
                .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(applicant1)
            .dueDate(LocalDate.of(2020, 5, 21))
            .application(
                Application.builder().issueDate(LocalDate.of(2020, 1, 1)).build()
            )
            .build();

        when(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()))
            .thenReturn(caseData.getApplication().getIssueDate().plusDays(141));

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("caseReference", formatId(TEST_CASE_ID));
        expectedEntries.put("applicant1FirstName", TEST_FIRST_NAME);
        expectedEntries.put("applicant1LastName", TEST_LAST_NAME);
        expectedEntries.put("applicant1Address", "Correspondence Address\nLine 2\nLine 3\nPost Town\nPost Code");
        expectedEntries.put("divorceOrCivilPartnershipEmail", "divorcecase@justice.gov.uk");
        expectedEntries.put("divorceOrEndCivilPartnershipApplication", "application to end your civil partnership");
        expectedEntries.put("issueDate", "1 January 2020");
        expectedEntries.put("relation", "civil partner");
        expectedEntries.put("waitUntilDate", "21 May 2020");
        expectedEntries.put("divorceOrEndCivilPartnershipProcess", "process to end your civil partnership");
        expectedEntries.put("divorceOrCivilPartnershipProceedings", "proceedings to end your civil partnership");
        expectedEntries.put("dueDate", "21 May 2020");
        expectedEntries.put("divorceOrCivilPartnershipServiceHeader", "End A Civil Partnership Service");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
