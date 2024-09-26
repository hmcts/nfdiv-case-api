package uk.gov.hmcts.divorce.document.print.model;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.util.List;
import java.util.UUID;

class PrintTest {

    @Test
    void shouldCreateRecipientlist() {
        final String caseId = UUID.randomUUID().toString();
        final String letterType = RandomStringUtils.random(20, true, false);
        final String recipientName = RandomStringUtils.random(20, true, false);

        Print print = new Print(null, caseId, null, letterType, recipientName, YesOrNo.NO);

        final List<String> recipients = print.getRecipients();
        final List<String> expected_recipients = List.of(caseId, recipientName, letterType);

        Assertions.assertLinesMatch(recipients, expected_recipients);
        Assertions.assertEquals(print.getIsInternational(), false);
    }

    @Test
    void shouldCreateRecipientlistWithInternationalFlagSet() {
        final String caseId = UUID.randomUUID().toString();
        final String letterType = RandomStringUtils.random(20, true, false);
        final String recipientName = RandomStringUtils.random(20, true, false);

        Print print = new Print(null, caseId, null, letterType, recipientName, YesOrNo.YES);

        final List<String> recipients = print.getRecipients();
        final List<String> expected_recipients = List.of(caseId, recipientName, letterType);

        Assertions.assertLinesMatch(recipients, expected_recipients);
        Assertions.assertEquals(print.getIsInternational(), true);
    }
}
