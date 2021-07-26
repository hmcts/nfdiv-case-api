package uk.gov.hmcts.divorce.divorcecase.task;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

class CaseTaskRunnerTest {

    @Test
    void shouldReturnReducedFunctionWrappedInCaseTaskRunnerAndBeAppliedToCaseDetails() {

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = caseTasks(
            cd -> {
                cd.getData().setApplicant1(Applicant.builder().firstName("first name").build());
                return cd;
            },
            new TestCaseTask()
        ).run(caseDetails);

        assertThat(result.getData().getApplicant1())
            .extracting(Applicant::getFirstName, Applicant::getLastName)
            .contains("first name", "last name");
    }
    
    public static class TestCaseTask implements CaseTask {

        @Override
        public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
            caseDetails.getData().getApplicant1().setLastName("last name");
            return caseDetails;
        }
    }
}
