package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FormatUtilTest {

    @Test
    void shouldFormatCaseIdForSettingInEmailTemplate() {
        assertThat(FormatUtil.formatId(1234567890123456L), is("1234-5678-9012-3456"));
    }
}