package uk.gov.hmcts.divorce.legaladvisor;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
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
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.JURISDICTION_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.PREVIOUS_PROCEEDINGS_DETAILS;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CO_REFUSAL_ORDER_WITH_AMENDMENTS_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CO_REFUSAL_ORDER_WITH_MORE_INFO_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class LegalAdvisorMakeDecisionFT extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision.json";
    private static final String JOINT_WELSH_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-joint-welsh.json";
    private static final String OFFLINE_CO_REJECTED_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-offline-rejected.json";
    private static final String OFFLINE_CO_CLARIFICATION_REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-offline-clarification.json";
    private static final String CO_MORE_INFO_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-more-info.json";
    private static final String CO_REJECTED_RESPONSE = "classpath:responses/response-legal-advisor-make-decision-co-rejected.json";
    private static final String OFFLINE_CO_REJECTED_SOLE_RESPONSE =
        "classpath:responses/response-legal-advisor-make-decision-co-rejected-offline-sole.json";
    private static final String OFFLINE_CO_REJECTED_JOINT_RESPONSE =
        "classpath:responses/response-legal-advisor-make-decision-co-rejected-offline-joint.json";
    private static final String OFFLINE_CO_CLARIFICATION_SOLE_RESPONSE =
        "classpath:responses/response-legal-advisor-make-decision-co-clarification-offline-sole.json";
    private static final String OFFLINE_CO_CLARIFICATION_JOINT_RESPONSE =
        "classpath:responses/response-legal-advisor-make-decision-co-clarification-offline-joint.json";
    private static final String REQUEST_JOINT_APPS_REPRESENTED
        = "classpath:request/casedata/ccd-callback-casedata-legal-advisor-make-decision-joint-apps-represented.json";
    private static final String CO_REJECTED_JOINT_APPS_REPRESENTED_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-rejected-joint-apps-represented.json";
    private static final String CO_REJECTED_JOINT_APPS_REPRESENTED_MORE_INFO_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-rejected-more-info-joint-apps-represented.json";

    private static final String CO_REJECTED_JOINT_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-rejected-joint.json";

    private static final String CO_MORE_INFO_MID_EVENT_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-more-info-mid-event.json";
    private static final String CO_REJECTED_MID_EVENT_RESPONSE
        = "classpath:responses/response-legal-advisor-make-decision-co-rejected-mid-event.json";

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private CaseDocumentAccessManagement caseDocumentAccessManagement;


    @Test
    public void shouldSendEmailToApp1SolicitorAndGenerateRefusalOrderWhenMoreInfoSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coRefusalClarificationReason", Set.of(PREVIOUS_PROCEEDINGS_DETAILS));

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
    public void shouldSendWelshNotificationsIfJointConditionalOrderNotGrantedForMoreInfo() throws IOException {
        Map<String, Object> request = caseData(JOINT_WELSH_REQUEST);
        request.put("coRefusalDecision", "moreInfo");
        request.put("coRefusalClarificationReason", Set.of(MARRIAGE_CERTIFICATE_TRANSLATION));

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @Disabled("CDAM requires the case to exist")
    public void shouldSendAwaitingAmendedApplicationLettersToOfflineApplicantOnlyIfSoleCase() throws IOException {
        Map<String, Object> request = caseData(OFFLINE_CO_REJECTED_REQUEST);

        final ListValue<DivorceDocument> miniApplicationListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .documentLink(uploadDocument())
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
    @Disabled("CDAM requires the case to exist")
    public void shouldSendAwaitingAmendedApplicationLettersToBothOfflineApplicantsIfJointCase() throws IOException {
        Map<String, Object> request = caseData(OFFLINE_CO_REJECTED_REQUEST);

        final ListValue<DivorceDocument> miniApplicationListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .documentLink(uploadDocument())
                .build())
            .build();

        request.put("documentsGenerated", singletonList(miniApplicationListValue));
        request.put("applicationType", "jointApplication");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_CO_REJECTED_JOINT_RESPONSE)));
    }

    @Test
    public void shouldSendAwaitingClarificationApplicationLettersToOfflineApplicantOnlyIfSoleCase() throws IOException {
        Map<String, Object> request = caseData(OFFLINE_CO_CLARIFICATION_REQUEST);

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_CO_CLARIFICATION_SOLE_RESPONSE)));
    }

    @Test
    public void shouldSendAwaitingClarificationApplicationLettersToBothOfflineApplicantsIfJointCase() throws IOException {
        Map<String, Object> request = caseData(OFFLINE_CO_CLARIFICATION_REQUEST);

        request.put("applicationType", "jointApplication");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(OFFLINE_CO_CLARIFICATION_JOINT_RESPONSE)));
    }

    @Test
    public void shouldSendEmailToBothApplicantSolicitorsAndGenerateRefusalOrderWhenRejectedSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST_JOINT_APPS_REPRESENTED);

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_JOINT_APPS_REPRESENTED_RESPONSE)));
    }

    @Test
    public void shouldSendEmailToBothApplicantsAndGenerateRefusalOrderWhenRejectedSelectedForJointApplication() throws IOException {
        Map<String, Object> request = caseData(JOINT_WELSH_REQUEST);
        request.put("coRefusalDecision", "reject");
        request.put("applicant1LanguagePreferenceWelsh", "No");
        request.put("applicant2LanguagePreferenceWelsh", "No");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_JOINT_RESPONSE)));
    }

    @Test
    public void shouldSendEmailInWelshToBothApplicantsAndGenerateRefusalOrderWhenRejectedSelectedForJointApplication() throws IOException {
        Map<String, Object> request = caseData(JOINT_WELSH_REQUEST);
        request.put("coRefusalDecision", "reject");
        request.put("applicant1LanguagePreferenceWelsh", "Yes");
        request.put("applicant2LanguagePreferenceWelsh", "Yes");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(CO_REJECTED_JOINT_RESPONSE));
        jsonDocument.set("data.applicant1LanguagePreferenceWelsh", "Yes");
        jsonDocument.set("data.applicant2LanguagePreferenceWelsh", "Yes");

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    public void shouldSendEmailToBothApplicantSolicitorsAndGenerateRefusalOrderWhenMoreInfoSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST_JOINT_APPS_REPRESENTED);
        request.put("coRefusalDecision", "moreInfo");
        request.put("coRefusalClarificationReason", Set.of(MARRIAGE_CERTIFICATE));

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_JOINT_APPS_REPRESENTED_MORE_INFO_RESPONSE)));
    }

    @Test
    public void midEventShouldGenerateRefusalOrderWhenMoreInfoSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coRefusalDecision", "moreInfo");
        request.put("coRefusalClarificationReason", Set.of(JURISDICTION_DETAILS));

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, CO_REFUSAL_ORDER_WITH_MORE_INFO_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_MORE_INFO_MID_EVENT_RESPONSE)));
    }

    @Test
    public void midEventShouldGenerateRefusalOrderWhenAmendApplicationSelected() throws IOException {
        Map<String, Object> request = caseData(REQUEST);
        request.put("coRefusalDecision", "reject");

        Response response = triggerCallback(request, LEGAL_ADVISOR_MAKE_DECISION, CO_REFUSAL_ORDER_WITH_AMENDMENTS_MID_EVENT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CO_REJECTED_MID_EVENT_RESPONSE)));
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
