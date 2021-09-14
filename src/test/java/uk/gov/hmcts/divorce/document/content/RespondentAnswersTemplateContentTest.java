package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class RespondentAnswersTemplateContentTest {
    @InjectMocks
    private RespondentAnswersTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForRespondentAnswers() {
        CaseData caseData = caseData();
        caseData.getAcknowledgementOfService().setLegalProceedingsDescription("some description");
        caseData.getAcknowledgementOfService().setJurisdictionAgree(YES);
        caseData.getAcknowledgementOfService().setLegalProceedingsExist(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build()
        );

        var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name("app1fname app1lname");
        marriageDetails.setApplicant2Name("app2fname app2lname");

        caseData.getApplication().setMarriageDetails(marriageDetails);

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(templateContentSupplier.get()).contains(
            entry(ISSUE_DATE, "28 April 2021"),
            entry(CCD_CASE_REFERENCE, TEST_CASE_ID),
            entry(APPLICANT_1_FULL_NAME, "app1fname app1lname"),
            entry(APPLICANT_2_FULL_NAME, "app2fname app2lname"),
            entry("respJurisdictionAgree", YES.getValue()),
            entry("respJurisdictionDisagreeReason", null),
            entry("respLegalProceedingsExist", YES.getValue()),
            entry("respLegalProceedingsDescription", "some description"),
            entry("respSolicitorRepresented", YES.getValue())
        );

    }
}
