package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_COSTS_CLAIM;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreateApplication.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationContactInformation;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SolicitorCreateApplicationTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String LANGUAGE_PREFERENCE_WELSH = "languagePreferenceWelsh";

    @Test
    public void shouldUpdateCaseDataWithClaimCostsAndCourtDetailsWhenAboutToSubmitCallbackIsSuccessful() throws Exception {
        Map<String, Object> caseData = caseData(REQUEST);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO);
        caseData.put(FINANCIAL_ORDER, NO);
        caseData.put("applicant2OrgContactInformation", organisationContactInformation());

        Response response = triggerCallback(caseData, SOLICITOR_CREATE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        // document_url and document_binary_url are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(
                "classpath:responses/response-solicitor-create-about-to-submit.json"
            )));
    }
}
