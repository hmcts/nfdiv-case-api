package uk.gov.hmcts.divorce.caseworker;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.DocumentManagementStore;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.document.domain.Document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAos.CASEWORKER_ISSUE_AOS;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseDataFromString;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsString;

@SpringBootTest
public class CaseworkerIssueAosTest extends FunctionalTestSuite {

    private static final String REQUEST_ABOUT_TO_SUBMIT =
        "classpath:request/casedata/ccd-callback-caseworker-issue-aos-about-to-submit.json";
    private static final String REQUEST_SUBMITTED =
        "classpath:request/casedata/ccd-callback-caseworker-issue-aos-submitted.json";

    @Autowired
    private DocumentManagementStore documentManagementStore;

    @Test
    public void shouldCompleteAboutToSubmitCallbackSuccessfully() throws Exception {

        final String caseDataAsString = resourceAsString(REQUEST_ABOUT_TO_SUBMIT);
        final Document divorceApplicationDocument = documentManagementStore.upload(
            "Divorce application",
            "divorce-application.pdf",
            "classpath:Test.pdf");
        final Document aosDocument = documentManagementStore.upload(
            "AOS",
            "aos.pdf",
            "classpath:Test.pdf");

        final String caseDataWithDocuments = caseDataAsString
            .replace("<$DIVORCE_APPLICATION_DOCUMENT_URL>", divorceApplicationDocument.links.self.href)
            .replace("<$DIVORCE_APPLICATION_BINARY_URL>", divorceApplicationDocument.links.binary.href)
            .replace("<$AOS_DOCUMENT_URL>", aosDocument.links.self.href)
            .replace("<$AOS_BINARY_URL>", aosDocument.links.binary.href);

        final Response response = triggerCallback(
            caseDataFromString(caseDataWithDocuments),
            CASEWORKER_ISSUE_AOS,
            ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
