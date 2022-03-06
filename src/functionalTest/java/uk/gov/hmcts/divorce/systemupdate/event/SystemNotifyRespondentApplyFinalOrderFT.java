package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyRespondentApplyFinalOrder.SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRemindApplicantsApplyForFinalOrderTask.NOTIFICATION_SENT_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.FINAL_ORDER_ELIGIBLE_FROM_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.FINAL_ORDER_ELIGIBLE_TO_RESPONDENT_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemNotifyRespondentApplyFinalOrderFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-notify-respondent-apply-final-order.json";
    private static final String RESPONSE = "classpath:responses/response-system-notify-respondent-apply-final-order.json";


    @Test
    public void shouldPassValidationAndSendEmailsToRespondent() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ELASTIC_SEARCH_ENABLED", matches = "true")
    public void shouldSearchForRespodentEligibleToApplyForFinalOrderCases() {
        final BoolQueryBuilder query =
            boolQuery()
                .must(matchQuery(STATE, AwaitingFinalOrder))
                .must(existsQuery(FINAL_ORDER_ELIGIBLE_FROM_DATE))
                .must(existsQuery(FINAL_ORDER_ELIGIBLE_TO_RESPONDENT_DATE))
                .filter(rangeQuery(FINAL_ORDER_ELIGIBLE_FROM_DATE)
                    .lte(LocalDate.now().minusDays(14)))
                .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES));

        List<CaseDetails> filteredCases = searchForCasesWithQuery(query);

        filteredCases.forEach(caseDetails -> {
            assertThat(caseDetails.getState().equals(AwaitingFinalOrder));
            CaseData caseData = getCaseData(caseDetails.getData());
            assertThat(caseData.getFinalOrder().getDateFinalOrderEligibleToRespondent()).isNotNull();
            assertThat(caseData.getFinalOrder().getDateFinalOrderEligibleFrom()).isNotNull();
            assertThat(caseData.getFinalOrder().getDateFinalOrderEligibleFrom().minusDays(14)).isBeforeOrEqualTo(LocalDate.now());
            assertThat(caseData.getFinalOrder().hasFinalOrderReminderSentApplicant1()).isFalse();
        });
    }
}
