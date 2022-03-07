package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getTemplateFormatDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
public class AosResponseLetterTemplateContentTest {

    @Mock
    private CommonContent commonContent;

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
            .application(
                Application.builder().issueDate(LocalDate.now()).build()
            )
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("husband");

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry("ccdCaseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nPost Code"),
            entry("divorceOrCivilPartnershipEmail", "divorcecase@justice.gov.uk"),
            entry("divorceOrEndCivilPartnershipApplication", "divorce application"),
            entry("issueDate", getTemplateFormatDate()),
            entry("relation", "husband")
        );
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
            .application(
                Application.builder().issueDate(LocalDate.now()).build()
            )
            .build();

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).contains(
            entry("ccdCaseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FirstName", TEST_FIRST_NAME),
            entry("applicant1LastName", TEST_LAST_NAME),
            entry("applicant1Address", "Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nPost Code"),
            entry("divorceOrCivilPartnershipEmail", "divorcecase@justice.gov.uk"),
            entry("divorceOrEndCivilPartnershipApplication", "application to end your civil partnership"),
            entry("issueDate", getTemplateFormatDate()),
            entry("relation", "civil partner")
        );
    }
}
