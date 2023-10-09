package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

class FormatUtilTest {

    @Test
    void shouldFormatCaseIdForSettingInEmailTemplate() {
        assertThat(FormatUtil.formatId(TEST_CASE_ID), is(FORMATTED_TEST_CASE_ID));
    }
}
