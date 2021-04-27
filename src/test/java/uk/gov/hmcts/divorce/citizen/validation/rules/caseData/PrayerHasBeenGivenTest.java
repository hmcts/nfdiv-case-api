package uk.gov.hmcts.divorce.citizen.validation.rules.caseData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class PrayerHasBeenGivenTest {

    private PrayerHasBeenGiven rule;
    private CaseData caseData;

    @BeforeEach
    private void setup() {
        rule = new PrayerHasBeenGiven();
        caseData = new CaseData();
    }

    @Test
    public void shouldReturnTrueWhenPrayerHasBeenGivenIsNull() {
        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenPrayerHasBeenGivenIsYes() {
        caseData.setPrayerHasBeenGiven(YesOrNo.YES);

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenPrayerHasBeenGivenIsNo() {
        caseData.setPrayerHasBeenGiven(YesOrNo.NO);

        rule.setCaseData(caseData);
        boolean result = rule.when();

        assertTrue(result);
    }

    @Test
    public void shouldReturnErrorMessageWithNull() {
        rule.setCaseData(caseData);

        rule.setResult(new ArrayList<>());
        rule.then();

        assertEquals("PrayerHasBeenGiven must be 'YES'. Actual data is: null", rule.getResult().get(0));
    }
}
