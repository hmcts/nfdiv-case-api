package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.ReIssueApplicationService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderRefused;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Disputed;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateIssue;

@Component
@Slf4j
public class CaseworkerReissueApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REISSUE_APPLICATION = "caseworker-reissue-application";

    @Autowired
    private ReIssueApplicationService reIssueApplicationService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REISSUE_APPLICATION)
            .forStates(
                AwaitingAos,AosDrafted, AosOverdue,
                Rejected, ConditionalOrderRefused, Withdrawn,
                Disputed, Holding, AwaitingDocuments, AwaitingService)
            .name("Reissue case")
            .description("Application Reissued")
            .showSummary()
            .explicitGrants()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(READ,
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR))
            .page("reissueApplication")
            .pageLabel("Reissue Divorce Application")
            .complex(CaseData::getApplication)
                .mandatory(Application::getReissueOption)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        log.info("Caseworker reissue application about to submit callback invoked for case id: {}", details.getId());

        final List<String> caseValidationErrors = validateIssue(details.getData());

        if (!isEmpty(caseValidationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(caseValidationErrors)
                .build();
        }

        final CaseDetails<CaseData, State> result = reIssueApplicationService.process(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(result.getData())
            .state(result.getState())
            .build();
    }
}
