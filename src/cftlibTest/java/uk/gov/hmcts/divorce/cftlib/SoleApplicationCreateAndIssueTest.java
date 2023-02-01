package uk.gov.hmcts.divorce.cftlib;

import org.junit.jupiter.api.Order;
import org.junitpioneer.jupiter.RetryingTest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.regex.Pattern.compile;
import static uk.gov.hmcts.divorce.cftlib.util.PlaywrightHelpers.LONG_WAIT;
import static uk.gov.hmcts.divorce.cftlib.util.PlaywrightHelpers.RETRIES;
import static uk.gov.hmcts.divorce.cftlib.util.PlaywrightHelpers.select;

public class SoleApplicationCreateAndIssueTest extends XuiTest {

    String caseRef;

    @RetryingTest(maxAttempts = RETRIES)
    @Order(1)
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
        // Click #applicant1SolicitorEmail]
        page.locator("#applicant1SolicitorEmail").click();
        // Fill #applicant1SolicitorEmail]
        page.locator("#applicant1SolicitorEmail").fill("james@mcdee.com");
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
        page.waitForURL(compile("http://localhost:3000/cases/case-details/.+"), LONG_WAIT);

        caseRef = page.url().substring(page.url().lastIndexOf("/") + 1);
    }

    @RetryingTest(maxAttempts = 3)
    @Order(2)
    public void submitCase() {
        signInWithSolicitor();
        page.navigate("http://localhost:3000/cases/case-details/" + caseRef);
        // Select 2: Object
        page.locator("select").selectOption(select("Sign and submit"));
        // Click text=Go
        page.locator("text=Go").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-details/" + caseRef + "/trigger/solicitor-submit-application/solicitor"
            + "-submit-applicationSolStatementOfTruth");
        // Check #solUrgentCase_No
        page.locator("#solUrgentCase_No").check();
        // Check #serviceMethod-solicitorService
        page.locator("#serviceMethod-solicitorService").check();
        // Check #serviceMethod-courtService
        page.locator("#serviceMethod-courtService").check();
        // Check #solStatementOfReconciliationCertify_Yes
        page.locator("#solStatementOfReconciliationCertify_Yes").check();
        // Check #solStatementOfReconciliationDiscussed_Yes
        page.locator("#solStatementOfReconciliationDiscussed_Yes").check();
        // Check input[name="applicant1PrayerDissolveDivorce"]
        page.locator("input[name=\"applicant1PrayerDissolveDivorce\"]").check();
        // Check input[name="applicant1PrayerFinancialOrdersThemselves"]
        page.locator("input[name=\"applicant1PrayerFinancialOrdersThemselves\"]").check();
        // Check input[name="applicant1PrayerFinancialOrdersChild"]
        page.locator("input[name=\"applicant1PrayerFinancialOrdersChild\"]").check();
        // Check #applicant1StatementOfTruth_Yes
        page.locator("#applicant1StatementOfTruth_Yes").check();
        // Click #solSignStatementOfTruth_radio >> text=Yes
        page.locator("#solSignStatementOfTruth_radio >> text=Yes").click();
        // Click #solStatementOfReconciliationName
        page.locator("#solStatementOfReconciliationName").click();
        // Fill #solStatementOfReconciliationName
        page.locator("#solStatementOfReconciliationName").fill("Billy");
        // Fill text=Name of your firmName of your firm is required >> input[type="text"]
        page.locator("#solStatementOfReconciliationFirm").fill("Bob");
        // Check #solPaymentHowToPay-feePayByAccount

        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-details/" + caseRef + "/trigger/solicitor-submit-application/solicitor"
            + "-submit-applicationSolPayment");

        page.locator("#solPaymentHowToPay-feePayByAccount").check();
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-details/" + caseRef + "/trigger/solicitor-submit-application/solicitor"
            + "-submit-applicationSolPayAccount");
        // Select 1: 05af69c6-72f8-478d-bd11-56f533e980e0
        page.locator("select").selectOption(select("PBA0077597"));
        // Click input[type="text"]
        page.locator("#feeAccountReference").click();
        // Fill input[type="text"]
        page.locator("#feeAccountReference").fill("123");
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-details/" + caseRef + "/trigger/solicitor-submit-application/solicitor"
            + "-submit-applicationSolPaymentSummary");

        page.locator("text=Continue").click();
        // Click text=Submit Application
        page.locator("text=Submit Application").click();
        assertThat(page).hasURL(compile("http://localhost:3000/cases/case-details/.+"));
    }

    @RetryingTest(maxAttempts = 3)
    @Order(3)
    public void issueCase() {
        signInWithCaseworker();
        page.navigate("http://localhost:3000/cases/case-details/" + caseRef);

        page.locator("select").selectOption(select("Application issue"));
        // Click text=Go
        page.locator("text=Go").click();
        // Click text=Country of marriageEnter the country in which the marriage took place >> input[type="text"]
        page.locator("text=Country of marriageEnter the country in which the marriage took place >> input[type=\"text\"]").click();
        // Fill text=Country of marriageEnter the country in which the marriage took place >> input[type="text"]
        page.locator("text=Country of marriageEnter the country in which the marriage took place >> input[type=\"text\"]").fill("England");
        // Click text=Place of marriageEnter the place of marriage as it appears on the certificate >> input[type="text"]
        page.locator("text=Place of marriageEnter the place of marriage as it appears on the certificate >> input[type=\"text\"]").click();
        // Fill text=Place of marriageEnter the place of marriage as it appears on the certificate >> input[type="text"]
        page.locator("text=Place of marriageEnter the place of marriage as it appears on the certificate >> input[type=\"text\"]").fill(
            "Norwich");
        // Click text=Continue
        page.locator("text=Continue").click();
        assertThat(page).hasURL("http://localhost:3000/cases/case-details/" + caseRef + "/trigger/caseworker-issue-application/submit");
        // Click text=Save and continue
        page.locator("text=Save and continue").click();
    }
}
