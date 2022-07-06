package uk.gov.hmcts.divorce.legaladvisor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.testutil.DocumentManagementStore;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.document.domain.Document;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class LegalAdvisorMakeDecisionFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision.json";
    private static final String JOINT_WELSH_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-joint-welsh.json";
    private static final String OFFLINE_CO_REJECTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-offline-rejected.json";
    private static final String CO_MORE_INFO_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-more-info.json";
    private static final String CO_REJECTED_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-rejected.json";
    private static final String OFFLINE_CO_REJECTED_SOLE_RESPONSE =
        "classpath:responses/response-legal-advisor-make-decision-co-rejected-offline-sole.json";
    private static final String OFFLINE_CO_REJECTED_JOINT_RESPONSE =
        "classpath:responses/response-legal-advisor-make-decision-co-rejected-offline-joint.json";


    @Autowired
    private DocumentManagementStore documentManagementStore;

    @Test
    public void shouldSendEmailToApp1SolicitorAndGenerateRefusalOrderWhenMoreInfoSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_MORE_INFO_RESPONSE)));
    }

    @Test
    public void shouldGenerateRefusalOrderWhenRejectedSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coRefusalDecision", "reject");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_RESPONSE)));
    }

    @Test
    public void shouldSendWelshNotificationsIfJointConditionalOrderNotGranted() throws IOException {
        Map<String, Object> request = caseData(JOINT_WELSH_REQUEST);
        request.put("coRefusalDecision", "moreInfo");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendAwaitingAmendedApplicationLettersToOfflineApplicantOnlyIfSoleCase() throws IOException {
        Map<String, Object> request = caseData(OFFLINE_CO_REJECTED_REQUEST);

        Document document = documentManagementStore.upload("", "draft-divorce-application-1234567890123456.pdf", "classpath:Test.pdf");

        final ListValue<DivorceDocument> miniApplicationListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .documentLink(new uk.gov.hmcts.ccd.sdk.type.Document(document.links.self.href, document.originalDocumentName, document.links.binary.href))
                .build())
            .build();

        request.put("documentsGenerated", singletonList(miniApplicationListValue));


        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_CO_REJECTED_SOLE_RESPONSE)));
    }

    @Test
    public void shouldSendAwaitingAmendedApplicationLettersToBothOfflineApplicantsIfJointCase() throws IOException {
        Map<String, Object> request = caseData(OFFLINE_CO_REJECTED_REQUEST);

        Document document = documentManagementStore.upload("", "draft-divorce-application-1234567890123456.pdf", "classpath:Test.pdf");

        final ListValue<DivorceDocument> miniApplicationListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .documentLink(new uk.gov.hmcts.ccd.sdk.type.Document(document.links.self.href, document.originalDocumentName, document.links.binary.href))
                .build())
            .build();

        request.put("documentsGenerated", singletonList(miniApplicationListValue));
        request.put("applicationType", "jointApplication");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        //TODO: remove println
        System.out.println(response.asString());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_CO_REJECTED_JOINT_RESPONSE)));
    }
}
