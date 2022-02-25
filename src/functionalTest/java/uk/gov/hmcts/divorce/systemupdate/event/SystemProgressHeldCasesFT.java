package uk.gov.hmcts.divorce.systemupdate.event;

import io.restassured.response.Response;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.CaseDataUtil.caseData;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;

@SpringBootTest
public class SystemProgressHeldCasesFT extends FunctionalTestSuite {

    private static final String REQUEST =
        "classpath:request/casedata/ccd-callback-casedata-system-progress-held-cases.json";

    @Test
    public void shouldPassValidationAndSendEmailsToJointApplicants() throws IOException {
        Map<String, Object> request = caseData(REQUEST);

        Response response = triggerCallback(request, SYSTEM_PROGRESS_HELD_CASE, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ELASTIC_SEARCH_ENABLED", matches = "true")
    @Disabled("Ccd has limitation of 10K cases and currently it is returning >10K")
    public void shouldSearchForCasesWhereHoldingPeriodHasEnded() {
        final BoolQueryBuilder query = boolQuery()
            .must(matchQuery(STATE, Holding))
            .filter(rangeQuery(CcdSearchService.DUE_DATE).lte(LocalDate.now()));

        searchForCasesWithQuery(query)
            .forEach(caseDetails -> {
                CaseData caseData = getCaseData(caseDetails.getData());
                assertThat(caseDetails.getState().equals(Holding));
                assertThat(caseData.getDueDate()).isBeforeOrEqualTo(LocalDate.now());
            });
    }
}
