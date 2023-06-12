package uk.gov.hmcts.divorce.cftlib;

import org.junitpioneer.jupiter.RetryingTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.regex.Pattern.compile;
import static uk.gov.hmcts.divorce.cftlib.util.PlaywrightHelpers.RETRIES;
import static uk.gov.hmcts.divorce.cftlib.util.PlaywrightHelpers.select;

public class CaseworkerAddNoteTest extends XuiTest {

    @RetryingTest(maxAttempts = RETRIES)
    public void addNote() {
        signInWithCaseworker();
        page.locator("text=Create case").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-filter");

        page.locator("select[name=\"case-type\"]").selectOption("NFD");
        page.locator("text=Start").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/create-paper-case/submit");

        page.locator("text=Save and continue").click();
        assertThat(page).hasURL(compile("http://localhost:3000/cases/case-details/*"));

        page.locator("select").selectOption(select("Add note"));
        page.locator("text=Go").click();
        page.locator("textarea").click();
        page.locator("textarea").fill("Note");
        page.locator("text=Submit").click();
        page.locator("text=Save and continue").click();

        assertThat(page).hasURL(compile("http://localhost:3000/cases/case-details/*"));
    }
}
