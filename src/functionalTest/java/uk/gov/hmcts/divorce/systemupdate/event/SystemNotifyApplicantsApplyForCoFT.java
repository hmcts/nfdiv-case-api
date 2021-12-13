package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantsApplyForCO.SYSTEM_NOTIFY_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder.SystemNotifyApplicantsApplyForCOtask.NOTIFICATION_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemNotifyApplicantsApplyForCoFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-notify-joint-applicants-conditional-order.json";
    private static final String RESPONSE = "classpath:responses/response-system-notify-joint-applicants-conditional-order.json";


    @Test
    public void shouldPassValidationAndSendEmailsToApplicantAndRespondent() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, SYSTEM_NOTIFY_APPLICANTS_CONDITIONAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ELASTIC_SEARCH_ENABLED", matches = "true")
    public void shouldSearchForCasesReadyToApplyForConditionalOrder() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, AwaitingConditionalOrder))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

        searchForCasesWithQuery(query)
            .forEach(caseDetails -> {
                CaseData caseData = getCaseData(caseDetails.getData());
                assertThat(caseDetails.getState().equals(AwaitingConditionalOrder));
                assertThat(caseData.getApplication().getJointApplicantsNotifiedCanApplyForConditionalOrder()).isNotEqualTo(YesOrNo.YES);
                assertThat(caseData.getDueDate()).isBeforeOrEqualTo(LocalDate.now());
            });
    }

}
