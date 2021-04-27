package uk.gov.hmcts.divorce.citizen.validation.rules.caseData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PetitionerLastNameTest {

    private PetitionerLastName rule;
    private CaseData caseData;

    @BeforeEach
    private void setup() {
        rule = new PetitionerLastName();
        caseData = new CaseData();
    }

    @Test
    public void shouldReturnTrueWhenPetitionerLastNameIsNull() {
        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenPetitionerLastNameIsNotNull() {
        caseData.setPetitionerLastName("Name");

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertFalse(result);
    }

    @Test
    public void shouldReturnErrorMessageWithNull() {
        rule.setCaseData(caseData);

        rule.setResult(new ArrayList<>());
        rule.then();

        assertEquals("PetitionerLastName can not be null or empty. Actual data is: null", rule.getResult().get(0));
    }
}
