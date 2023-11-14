package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.CaseDocumentAccessManagement;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.divorce.testutil.IdamTokenGenerator;
import uk.gov.hmcts.divorce.testutil.ServiceAuthenticationGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorUpdateApplication.SOLICITOR_UPDATE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationContactInformation;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SolicitorUpdateApplicationFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-solicitor-update.json";

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private CaseDocumentAccessManagement caseDocumentAccessManagement;


    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldUpdateCaseDataWhenAboutToSubmitCallbackIsSuccessful() throws Exception {

        final Map<String, Object> caseData = caseData(REQUEST);
        caseData.put("applicationType", SOLE_APPLICATION);
        caseData.put("jurisdictionConnections", List.of(APP_1_APP_2_RESIDENT));
        caseData.put("applicant2OrgContactInformation", organisationContactInformation());

        final ListValue<DivorceDocument> miniApplicationListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .documentLink(uploadDocument())
                .build())
            .build();

        caseData.put("documentsGenerated", singletonList(miniApplicationListValue));

        final Response response = triggerCallback(caseData, SOLICITOR_UPDATE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-solicitor-update-about-to-submit.json"
            )));
    }

    private uk.gov.hmcts.ccd.sdk.type.Document uploadDocument() throws IOException {
        var document = caseDocumentAccessManagement.upload(
            idamTokenGenerator.generateIdamTokenForSystem(),
            serviceAuthenticationGenerator.generate(),
            "",
            "draft-divorce-application-1234567890123456.pdf",
            "classpath:Test.pdf"
        ).getDocuments().get(0);
        return new uk.gov.hmcts.ccd.sdk.type.Document(
            document.links.self.href,
            document.originalDocumentName,
            document.links.binary.href
        );
    }
}
