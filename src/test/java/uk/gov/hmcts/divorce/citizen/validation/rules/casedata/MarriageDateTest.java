package uk.gov.hmcts.divorce.citizen.validation.rules.casedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarriageDateTest {

    private MarriageDate rule;
    private CaseData caseData;

    @BeforeEach
    public void setup() {
        rule = new MarriageDate();
        caseData = new CaseData();
    }

    @Test
    public void whenShouldReturnTrueWhenMarriageDateIsNull() {
        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void whenShouldReturnTrueWhenMarriageDateLessThanOneYearAgo() {
        LocalDate marriageDate = LocalDate.now().minus(100, ChronoUnit.DAYS);
        caseData.setMarriageDate(marriageDate);

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void whenShouldReturnTrueWhenMarriageDateMoreThanOneHundredYearsAgo() {
        LocalDate marriageDate = LocalDate.now().minus(365 * 105, ChronoUnit.DAYS);
        caseData.setMarriageDate(marriageDate);

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void whenShouldReturnTrueWhenMarriageDateIsInTheFuture() {
        LocalDate marriageDate = LocalDate.now().plus(100, ChronoUnit.DAYS);
        caseData.setMarriageDate(marriageDate);

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void whenShouldReturnFalseWhenMarriageDateIsValid() {
        LocalDate marriageDate = LocalDate.now().minus(365 * 2, ChronoUnit.DAYS);
        caseData.setMarriageDate(marriageDate);

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertFalse(result);
    }

    @Test
    public void thenShouldReturnErrorMessageWithNull() {
        rule.setCaseData(caseData);

        rule.setResult(new ArrayList<>());
        rule.then();

        assertEquals("MarriageDate can not be null or empty. Actual data is: null", rule.getResult().get(0));
    }

    @Test
    public void thenShouldReturnErrorMessageWhenMarriageDateLessThanOneYearAgo() {
        LocalDate marriageDate = LocalDate.now().minus(100, ChronoUnit.DAYS);
        caseData.setMarriageDate(marriageDate);
        rule.setCaseData(caseData);

        rule.setResult(new ArrayList<>());
        rule.then();

        assertEquals("MarriageDate can not be less than one year ago. Actual data is: ".concat(String.valueOf(marriageDate)),
            rule.getResult().get(0));
    }

    @Test
    public void thenShouldReturnErrorMessageWhenMarriageDateMoreThanOneHundredYearsAgo() {
        LocalDate marriageDate = LocalDate.now().minus(365 * 105, ChronoUnit.DAYS);
        caseData.setMarriageDate(marriageDate);

        rule.setCaseData(caseData);

        rule.setResult(new ArrayList<>());
        rule.then();

        assertEquals("MarriageDate can not be more than 100 years ago. Actual data is: ".concat(String.valueOf(marriageDate)),
            rule.getResult().get(0));
    }

    @Test
    public void thenShouldReturnErrorMessageWhenMarriageDateIsInTheFuture() {
        LocalDate marriageDate = LocalDate.now().plus(100, ChronoUnit.DAYS);
        caseData.setMarriageDate(marriageDate);

        rule.setCaseData(caseData);

        rule.setResult(new ArrayList<>());
        rule.then();

        assertEquals("MarriageDate can not be in the future. Actual data is: ".concat(String.valueOf(marriageDate)),
            rule.getResult().get(0));
    }
}
