package uk.gov.hmcts.divorce.citizen.validation.rules.casedata;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.util.List;
import java.util.Optional;

@Data
@Rule
public class StatementOfTruth {

    private static final String BLANK_SPACE = " ";
    private static final String ACTUAL_DATA = "Actual data is: %s";
    private static final String ERROR_MESSAGE = "StatementOfTruth must be 'YES'.";

    @Result
    public List<String> result;

    @Given("caseData")
    public CaseData caseData = new CaseData();

    @When
    public boolean when() {
        return Optional.ofNullable(caseData.getStatementOfTruth()).isEmpty() || caseData.getStatementOfTruth().equals(YesOrNo.NO);
    }

    @Then
    public void then() {
        result.add(String.join(
            BLANK_SPACE, // delimiter
            ERROR_MESSAGE,
            String.format(ACTUAL_DATA, caseData.getStatementOfTruth())
        ));
    }
}
