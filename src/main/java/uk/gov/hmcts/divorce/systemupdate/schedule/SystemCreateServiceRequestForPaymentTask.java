package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateServiceRequest.CITIZEN_CREATE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemCreateServiceRequestForPaymentTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public static final String APPLICATION_FEE_ORDER_SUMMARY = "applicationFeeOrderSummary";
    public static final String APPLICATION_FEE_SERVICE_REQUEST = "applicationFeeServiceRequestReference";

    public static final String FINAL_ORDER_FEE_ORDER_SUMMARY = "applicant2FinalOrderFeeOrderSummary";
    public static final String FINAL_ORDER_FEE_SERVICE_REQUEST = "applicant2FinalOrderFeeServiceRequestReference";

    @Override
    public void run() {
        log.info("Create service request for payment scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .should(
                        boolQuery()
                            .must(matchQuery(STATE, AwaitingPayment))
                            .must(existsQuery(String.format(DATA, APPLICATION_FEE_ORDER_SUMMARY)))
                            .mustNot(existsQuery(String.format(DATA, APPLICATION_FEE_SERVICE_REQUEST)))
                    )
                    .should(
                        boolQuery()
                            .must(matchQuery(STATE, AwaitingFinalOrderPayment))
                            .must(existsQuery(String.format(DATA, FINAL_ORDER_FEE_ORDER_SUMMARY)))
                            .mustNot(existsQuery(String.format(DATA, FINAL_ORDER_FEE_SERVICE_REQUEST)))
                    )
                    .minimumShouldMatch(1);


            final List<CaseDetails> casesAwaitingPaymentWithoutServiceRequest =
                ccdSearchService.searchForAllCasesWithQuery(
                    query, user, serviceAuth, AwaitingPayment, AwaitingFinalOrderPayment
                );

            for (final CaseDetails caseDetails : casesAwaitingPaymentWithoutServiceRequest) {
                triggerCitizenCreateServiceRequest(user, serviceAuth, caseDetails);
            }

            log.info("Create service request for payment scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Create service request for payment schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Create service request for payment schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void triggerCitizenCreateServiceRequest(User user, String serviceAuth, CaseDetails caseDetails) {
        try {
            log.info("Submitting Create Service Request Event for Case {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), CITIZEN_CREATE_SERVICE_REQUEST, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
