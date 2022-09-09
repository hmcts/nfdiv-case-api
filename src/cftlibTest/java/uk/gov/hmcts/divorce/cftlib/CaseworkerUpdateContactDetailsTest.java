package uk.gov.hmcts.divorce.cftlib;

import org.junitpioneer.jupiter.RetryingTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.regex.Pattern.compile;
import static uk.gov.hmcts.divorce.cftlib.util.PlaywrightHelpers.RETRIES;
import static uk.gov.hmcts.divorce.cftlib.util.PlaywrightHelpers.select;

public class CaseworkerUpdateContactDetailsTest extends XuiTest {

    @RetryingTest(maxAttempts = RETRIES)
    public void updateContactDetails() {
        signInWithCaseworker();
        // Click text=Create case
        page.locator("text=Create case").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-filter");
        // Select NFD
        page.locator("select[name=\"case-type\"]").selectOption("NFD");
        // Click text=Start
        page.locator("text=Start").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/create-paper-case/submit");
        // Click text=Save and continue
        page.locator("text=Save and continue").click();
        assertThat(page).hasURL(compile("http://localhost:3000/cases/case-details/*"));
        // Select 19: Object
        page.locator("select").selectOption(select("Update contact details"));
        // Click text=Go
        page.locator("text=Go").click();
        // Click #applicant1FirstName
        page.locator("#applicant1FirstName").click();
        // Fill #applicant1FirstName
        page.locator("#applicant1FirstName").fill("James");
        // Press Tab
        page.locator("#applicant1FirstName").press("Tab");
        // Fill text=Applicant 1’s middle name (Optional)If they have a middle name then you must ent >> input[type="text"]
        page.locator("text=Applicant 1’s middle name (Optional)If they have a middle name then you must ent >> input[type=\"text\"]")
            .fill("McDee");
        // Press Tab
        page.locator("text=Applicant 1’s middle name (Optional)If they have a middle name then you must ent >> input[type=\"text\"]")
            .press("Tab");
        // Fill #applicant1LastName
        page.locator("#applicant1LastName").fill("Charles");
        // Check #applicant1Gender-male
        page.locator("#applicant1Gender-male").check();
        // Check #applicant1ContactDetailsType-private
        page.locator("#applicant1ContactDetailsType-private").check();
        // Click text=Applicant 1’s home address (Optional)Enter a UK postcodeFind addressI can't ente >> input[name="postcode"]
        page.locator("text=Applicant 1’s home address (Optional)Enter a UK postcodeFind addressI can't ente >> input[name=\"postcode\"]")
            .click();
        // Fill text=Applicant 1’s home address (Optional)Enter a UK postcodeFind addressI can't ente >> input[name="postcode"]
        page.locator("text=Applicant 1’s home address (Optional)Enter a UK postcodeFind addressI can't ente >> input[name=\"postcode\"]")
            .fill("NR21NR");
        // Click #applicant1Address_applicant1Address_postcodeLookup >> text=Find address
        page.locator("#applicant1Address_applicant1Address_postcodeLookup >> text=Find address").click();
        // Select 1: Object
        page.locator("select[name=\"address\"]").selectOption("1: Object");
        // Click #applicant2FirstName
        page.locator("#applicant2FirstName").click();
        // Fill #applicant2FirstName
        page.locator("#applicant2FirstName").fill("Jeremy");
        // Press Tab
        page.locator("#applicant2FirstName").press("Tab");
        // Press Tab
        page.locator("text=Applicant 2's middle name (Optional)If they have a middle name then you must ent >> input[type=\"text\"]")
            .press("Tab");
        // Fill #applicant2LastName
        page.locator("#applicant2LastName").fill("Long");
        // Press Tab
        page.locator("#applicant2LastName").press("Tab");
        // Check #applicant2Gender-male
        page.locator("#applicant2Gender-male").check();
        // Check #applicant2ContactDetailsType-private
        page.locator("#applicant2ContactDetailsType-private").check();
        // Check #applicant2SolicitorRepresented_No
        page.locator("#applicant2SolicitorRepresented_No").check();
        // Click text=Continue
        page.locator("text=Continue").click();
        // Click text=Save and continue
        page.locator("text=Save and continue").click();
        assertThat(page).hasURL(compile("http://localhost:3000/cases/case-details/*"));
    }
}
