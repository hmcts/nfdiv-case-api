package uk.gov.hmcts.divorce.cftlib;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class XuiTest extends CftlibTest {

    Playwright playwright;
    Browser browser;

    @BeforeAll
    void launchBrowser() {
        playwright = Playwright.create();
        // browser = playwright.chromium().launch();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
    }

    @AfterAll
    void closeBrowser() {
        playwright.close();
    }

    BrowserContext context;
    Page page;

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    void signInWith(String username) {
        page.navigate("http://localhost:3000");

        page.locator("[placeholder=\"Enter Username\"]").fill(username);
        page.locator("[placeholder=\"Enter Password\"]").fill("anythingWillWork");
        page.locator("text=Sign in").click();
        assertThat(page).hasURL("http://localhost:3000/cases");
    }

    void signInWithCaseworker() {
        signInWith("DivCaseWorkerUser@AAT.com");
    }

    void signInWithSolicitor() {
        signInWith("TEST_SOLICITOR@mailinator.com");
    }

}
