package uk.gov.hmcts.divorce.divorcecase.task;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class CaseTaskRunnerTest {

    @Test
    void shouldRunTaskFunctionWithCaseDetails() {

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final Function<CaseDetails<CaseData, State>, CaseDetails<CaseData, State>> setFirstName = cd -> {
            cd.getData().setApplicant1(Applicant.builder().firstName("first name").build());
            return cd;
        };

        final CaseDetails<CaseData, State> result = new CaseTaskRunner(setFirstName).run(caseDetails);

        assertThat(result.getData().getApplicant1().getFirstName()).isEqualTo("first name");
    }
}
