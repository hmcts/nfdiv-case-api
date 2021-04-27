package uk.gov.hmcts.divorce.citizen.validation.rules.casedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PetitionerFirstNameTest {

    private PetitionerFirstName rule;
    private CaseData caseData;

    @BeforeEach
    private void setup() {
        rule = new PetitionerFirstName();
        caseData = new CaseData();
    }

    @Test
    public void shouldReturnTrueWhenPetitionerFirstNameIsNull() {
        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenPetitionerFirstNameIsNotNull() {
        caseData.setPetitionerFirstName("Name");

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertFalse(result);
    }

    @Test
    public void shouldReturnErrorMessageWithNull() {
        rule.setCaseData(caseData);

        rule.setResult(new ArrayList<>());
        rule.then();

        assertEquals("PetitionerFirstName can not be null or empty. Actual data is: null", rule.getResult().get(0));
    }
}
