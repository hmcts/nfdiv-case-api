package uk.gov.hmcts.divorce.cftlib;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SolicitorCreateCaseTest extends XuiTest {

    @Test
    @RetryingTest(maxAttempts = 3)
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
        // Click text=Search for an organisation You can only search for organisations already registe >> input[type="text"]
        page.locator("text=Search for an organisation You can only search for organisations already registe >> input[type=\"text\"]")
            .click();
        // Fill text=Search for an organisation You can only search for organisations already registe >> input[type="text"]
        page.locator("text=Search for an organisation You can only search for organisations already registe >> input[type=\"text\"]")
            .fill("org");
        // Click text=38 Delisle RoadLondonLondonSE28 0JESelect >> a
        page.locator("text=38 Delisle RoadLondonLondonSE28 0JESelect >> a").click();

        page.locator("text=Continue").click();

        // Check #applicant1ScreenHasMarriageBroken_Yes
        page.locator("#applicant1ScreenHasMarriageBroken_Yes").check();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationSolAboutApplicant1");
        // Click #applicant1FirstName
        page.locator("#applicant1FirstName").click();
        // Fill #applicant1FirstName
        page.locator("#applicant1FirstName").fill("Jill");
        // Press Tab
        page.locator("#applicant1FirstName").press("Tab");
        // Press Tab
        page.locator("text=Applicant’s middle name (Optional)If they have a middle name then you must enter >> input[type=\"text\"]")
            .press("Tab");
        // Fill #applicant1LastName
        page.locator("#applicant1LastName").fill("James");
        // Press Tab
        page.locator("#applicant1LastName").press("Tab");
        // Press ArrowLeft
        page.locator("#applicant1NameDifferentToMarriageCertificate_Yes").press("ArrowRight");
        // Select 1: husband
        page.locator("select").selectOption("1: husband");
        // Check #marriageFormationType-oppositeSexCouple
        page.locator("#marriageFormationType-oppositeSexCouple").check();
        // Double click input[type="email"]
        page.locator("input[type=\"email\"]").dblclick();
        // Fill input[type="email"]
        page.locator("input[type=\"email\"]").fill("michael@james.com");
        // Press Tab
        page.locator("input[type=\"email\"]").press("Tab");
        // Click input[name="postcode"]
        page.locator("input[name=\"postcode\"]").click();
        // Fill input[name="postcode"]
        page.locator("input[name=\"postcode\"]").fill("NR21NR");
        // Click text=Find address
        page.locator("text=Find address").click();
        // Select 1: Object
        page.locator("select[name=\"address\"]").selectOption("1: Object");
        // Click text=Do not need to keep contact details private
        page.locator("text=Do not need to keep contact details private").click();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationSolAboutApplicant2");
        // Click #applicant2FirstName
        page.locator("#applicant2FirstName").click();
        // Fill #applicant2FirstName
        page.locator("#applicant2FirstName").fill("Billy");
        // Press Tab
        page.locator("#applicant2FirstName").press("Tab");
        // Press Tab
        page.locator("text=Respondent's middle name (Optional)If they have a middle name then you must ente >> input[type=\"text\"]")
            .press("Tab");
        // Fill #applicant2LastName
        page.locator("#applicant2LastName").fill("Bob");
        // Click #applicant2NameDifferentToMarriageCertificate_radio >> text=No
        page.locator("#applicant2NameDifferentToMarriageCertificate_radio >> text=No").click();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationApplicant2ServiceDetails");
        // Check #applicant2SolicitorRepresented_No
        page.locator("#applicant2SolicitorRepresented_No").check();
        // Click input[name="postcode"]
        page.locator("input[name=\"postcode\"]").click();
        // Fill input[name="postcode"]
        page.locator("input[name=\"postcode\"]").fill("NR21NR");
        // Click text=Find address
        page.locator("text=Find address").click();
        // Select 1: Object
        page.locator("select[name=\"address\"]").selectOption("1: Object");
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationMarriageCertificateDetails");
        // Click input[name="marriageDate-day"]
        page.locator("input[name=\"marriageDate-day\"]").click();
        // Fill input[name="marriageDate-day"]
        page.locator("input[name=\"marriageDate-day\"]").fill("1");
        // Click input[name="marriageDate-month"]
        page.locator("input[name=\"marriageDate-month\"]").click();
        // Fill input[name="marriageDate-month"]
        page.locator("input[name=\"marriageDate-month\"]").fill("1");
        // Click input[name="marriageDate-year"]
        page.locator("input[name=\"marriageDate-year\"]").click();
        // Fill input[name="marriageDate-year"]
        page.locator("input[name=\"marriageDate-year\"]").fill("2000");
        // Press Tab
        page.locator("input[name=\"marriageDate-year\"]").press("Tab");
        // Fill text=Applicant’s full nameExactly as it appears on the certificate. Include any addit >> input[type="text"]
        page.locator("text=Applicant’s full nameExactly as it appears on the certificate. Include any addit >> input[type=\"text\"]")
            .fill("Billy");
        // Press Tab
        page.locator("text=Applicant’s full nameExactly as it appears on the certificate. Include any addit >> input[type=\"text\"]")
            .press("Tab");
        // Fill text=Respondent's full nameExactly as it appears on the certificate. Include any addi >> input[type="text"]
        page.locator("text=Respondent's full nameExactly as it appears on the certificate. Include any addi >> input[type=\"text\"]")
            .fill("Bob");
        // Check #marriageMarriedInUk_Yes
        page.locator("#marriageMarriedInUk_Yes").check();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationJurisdictionApplyForDivorce");
        // Check #jurisdictionConnections-A
        page.locator("#jurisdictionConnections-A").check();
        // Click #jurisdictionConnections div:has-text("Both parties to the marriage or civil partnership are habitually resident in Eng")
        page.locator("#jurisdictionConnections div:has-text(\"Both parties to the marriage or civil partnership are habitually resident "
            + "in Eng\")").click();
        // Check #jurisdictionConnections-B
        page.locator("#jurisdictionConnections-B").check();
        // Check #jurisdictionConnections-A
        page.locator("#jurisdictionConnections-A").check();
        // Check #jurisdictionConnections-C
        page.locator("#jurisdictionConnections-C").check();
        // Check #jurisdictionConnections-C2
        page.locator("#jurisdictionConnections-C2").check();
        // Click button:has-text("Continue")
        page.locator("button:has-text(\"Continue\")").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationOtherLegalProceedings");
        // Check #applicant1LegalProceedings_No
        page.locator("#applicant1LegalProceedings_No").check();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationFinancialOrders");
        // Check #applicant1FinancialOrder_No
        page.locator("#applicant1FinancialOrder_No").check();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/solicitor-create-appli"
            + "cationUploadSupportingDocuments");
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-create/DIVORCE/NFD/solicitor-create-application/submit");
        // Click text=Save Application
        page.locator("text=Save Application").click();
    }
}
