package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Value;
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
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantDisputeFormOverdue.SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyApplicantDisputeFormOverdueTask.NOTIFICATION_SENT_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AOS_RESPONSE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@SpringBootTest
public class SystemNotifyApplicantDisputeFormOverdueFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-notify-applicant-dispute-form-overdue.json";
    private static final String RESPONSE = "classpath:responses/response-system-notify-applicant-dispute-form-overdue.json";

    @Value("${submit_aos.dispute_offset_days}")
    private int disputeDueDateOffsetDays;

    @Test
    public void shouldNotifyApplicantIfDisputeFormOverdue() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());

        assertThatJson(response.asString())
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(RESPONSE)));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ELASTIC_SEARCH_ENABLED", matches = "true")
    public void shouldSearchForDisputeFormOverdueCases() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Holding))
            .must(matchQuery(AOS_RESPONSE, DISPUTE_DIVORCE.getType()))
            .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now().minusDays(disputeDueDateOffsetDays)))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES));

        searchForCasesWithQuery(query)
            .forEach(caseDetails -> {
                assertThat(caseDetails.getState().equals(Holding));
                CaseData caseData = getCaseData(caseDetails.getData());
                assertThat(DISPUTE_DIVORCE.getType().equals(caseData.getAcknowledgementOfService().getHowToRespondApplication()));
                assertThat(caseData.getApplication().getIssueDate().plusDays(10)).isBeforeOrEqualTo(LocalDate.now());
                assertThat(caseData.getAcknowledgementOfService().getApplicantNotifiedDisputeFormOverdue()).isNotEqualTo(YesOrNo.YES);
            });
    }
}
