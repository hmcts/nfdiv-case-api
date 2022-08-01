package uk.gov.hmcts.divorce.cftlib;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.regex.Pattern.compile;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class XuiTest extends CftlibTest {


    @Test
    public void logsIn() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            // Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
            Page page = browser.newPage();
            page.navigate("http://localhost:3000");

            // page.pause();
            // page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("/tmp/example.png")));

            page.locator("[placeholder=\"Enter Username\"]").fill("DivCaseWorkerUser@AAT.com");
            page.locator("[placeholder=\"Enter Password\"]").fill("anythingWillWork");
            page.locator("text=Sign in").click();
            assertThat(page).hasURL("http://localhost:3000/cases");

            page.locator("text=Create case").click();
            assertThat(page).hasURL("http://localhost:3000/cases/case-filter");

            page.locator("select[name=\"case-type\"]").selectOption("NFD");

            page.locator("text=Start").click();
            assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/create-paper-case/submit");

            page.locator("text=Save and continue").click();
            assertThat(page).hasURL(compile("http://localhost:3000/cases/case-details/*"));

            page.locator("select").selectOption("1: Object");
            page.locator("text=Go").click();
            page.locator("textarea").click();
            page.locator("textarea").fill("Note");
            page.locator("text=Continue").click();
            page.locator("text=Save and continue").click();

            assertThat(page).hasURL(compile("http://localhost:3000/cases/case-details/*"));
        }
    }

}
