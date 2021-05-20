package uk.gov.hmcts.divorce.solicitor;

import io.restassured.response.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.util.Map;
import java.util.stream.Stream;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.APPLICANT_1_ORGANISATION_POLICY;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.DIVORCE_COSTS_CLAIM;
import static uk.gov.hmcts.divorce.ccd.search.CaseFieldsConstants.FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreate.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorUpdateContactDetails.SOLICITOR_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_THE_SOL_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_UPDATE_CONTACT_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SolicitorMidEventCallbackTest extends FunctionalTestSuite {

    private static final String REQUEST = "classpath:request/casedata/ccd-callback-casedata.json";
    private static final String LANGUAGE_PREFERENCE_WELSH = "languagePreferenceWelsh";

    @ParameterizedTest
    @MethodSource("midEventUrlAndEventIdParameters")
    public void shouldTriggerMidEventCallbackAndReturnNoErrorsWhenSolicitorSelectsOrgToWhichTheyBelong(
        String midEventUrl,
        String eventId
    ) throws Exception {
        Map<String, Object> caseData = caseData(REQUEST);
        caseData.put(DIVORCE_COSTS_CLAIM, YES);
        caseData.put(LANGUAGE_PREFERENCE_WELSH, NO);
        caseData.put(FINANCIAL_ORDER, NO);
        caseData.put(APPLICANT_1_ORGANISATION_POLICY, organisationPolicy());

        Response response = triggerCallback(caseData, eventId, midEventUrl);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .isEqualTo(json(expectedResponse(
                "classpath:responses/ccd-callback-solicitor-create-mid-event.json"
            )));
    }

    private OrganisationPolicy<UserRole> organisationPolicy() {
        return OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationName("Test Organisation")
                .organisationId("HEBFGBR")
                .build())
            .build();
    }

    private static Stream<Arguments> midEventUrlAndEventIdParameters() {
        return Stream.of(
            Arguments.of(ABOUT_THE_SOL_MID_EVENT_URL, SOLICITOR_CREATE),
            Arguments.of(SOLICITOR_UPDATE_CONTACT_MID_EVENT_URL, SOLICITOR_UPDATE_CONTACT_DETAILS)
        );
    }
}
