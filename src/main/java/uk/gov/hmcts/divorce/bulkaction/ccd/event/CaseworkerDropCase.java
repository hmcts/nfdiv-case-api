package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Dropped;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;

@Component
@Slf4j
public class CaseworkerDropCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CASEWORKER_DROP_CASE = "caseworker-drop-case";

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    @Autowired
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_DROP_CASE)
            .forStateTransition(EnumSet.of(Created, Listed), Dropped)
            .name("Drop bulk case")
            .description("Drop bulk case")
            .showSummary()
            .showEventNotes()
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE));
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("Unlinking bulk case started for case id {} ", bulkCaseDetails.getId());

        bulkCaseProcessingService.updateAllBulkCases(
            bulkCaseDetails,
            SYSTEM_REMOVE_BULK_CASE,
            bulkCaseCaseTaskFactory.getCaseTask(bulkCaseDetails.getData(), SYSTEM_REMOVE_BULK_CASE),
            idamService.retrieveUser(request.getHeader(AUTHORIZATION)),
            authTokenGenerator.generate()
        );

        log.info("Unlinking bulk case completed for case id {} ", bulkCaseDetails.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
