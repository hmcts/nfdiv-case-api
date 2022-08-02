package uk.gov.hmcts.divorce.cftlib;

import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.regex.Pattern.compile;

public class SolicitorCreateCaseTest extends XuiTest {

    @Test
    public void createCase() {
        signInWithSolicitor();

        // Click text=Create case
        page.locator("text=Create case").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-filter");
        // Select NFD
        page.locator("select[name=\"case-type\"]").selectOption("NFD");
        // Select solicitor-create-application
        page.locator("select[name=\"event\"]").selectOption("solicitor-create-application");
        // Click text=Start
        page.locator("text=Start").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-applicationhowDoYouWantToApplyForDivorce");
        // Check #divorceOrDissolution-divorce
        page.locator("#divorceOrDissolution-divorce").check();
        // Click label:has-text("Sole Application")
        page.locator("label:has-text(\"Sole Application\")").click();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-applicationSolAboutTheSolicitor");
        // Click #applicant1SolicitorName
        page.locator("#applicant1SolicitorName").click();
        // Fill #applicant1SolicitorName
        page.locator("#applicant1SolicitorName").fill("James McDee");
        // Click input[type="email"]
        page.locator("input[type=\"email\"]").click();
        // Fill input[type="email"]
        page.locator("input[type=\"email\"]").fill("james@mcdee.com");
        // Click text=I confirm I’m willing to accept service of all correspondence and orders by emai
        page.locator("text=I confirm I’m willing to accept service of all correspondence and orders by emai").click();
        // Click text=Continue
        page.locator("text=Continue").click();
    }
}
