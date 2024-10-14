package uk.gov.hmcts.divorce.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerRemoveCasesFromBulkList.CASEWORKER_REMOVE_CASES_BULK_LIST;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentUtil.removeDocumentsBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

@Component
@Slf4j
public class CaseworkerRescindConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String RESCIND_CONDITIONAL_ORDER = "rescind-conditional-order";

    @Autowired
    private Clock clock;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(RESCIND_CONDITIONAL_ORDER)
            .forStates(
                GeneralConsiderationComplete, AwaitingFinalOrder, AwaitingFinalOrderPayment,
                ConditionalOrderPronounced, AwaitingPronouncement
            )
            .name("Rescind Conditional order")
            .description("Rescind Conditional order")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, CITIZEN, JUDGE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Rescind Conditional order about to submit callback invoked for case id: {}", details.getId());

        final CaseData caseData = details.getData();

        caseData.getConditionalOrder().setRescindedDate(LocalDate.now(clock));

        removeConditionalOrderDocuments(caseData);

        if (caseData.getBulkListCaseReferenceLink() != null) {
            removeCaseFromBulkList(details);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private void removeConditionalOrderDocuments(final CaseData caseData) {
        removeDocumentsBasedOnContactPrivacy(caseData, CONDITIONAL_ORDER_GRANTED);

        if (caseData.getApplicant1().isApplicantOffline()) {
            removeDocumentsBasedOnContactPrivacy(caseData, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);
        }

        if (caseData.getApplicant2().isApplicantOffline()) {
            removeDocumentsBasedOnContactPrivacy(caseData, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);
        }

        caseData.getConditionalOrder().setConditionalOrderGrantedDocument(null);
    }

    private void removeCaseFromBulkList(final CaseDetails<CaseData, State> details) {
        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String serviceAuthorization = authTokenGenerator.generate();

        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails
            = ccdSearchService.searchForBulkCaseById(
                details.getData().getBulkListCaseReferenceLink().getCaseReference(), user, serviceAuthorization);

        if (bulkCaseDetails != null) {
            boolean caseRemovedFromCasesAcceptedToListForHearing =
                !isEmpty(bulkCaseDetails.getData().getCasesAcceptedToListForHearing())
                    && bulkCaseDetails.getData().getCasesAcceptedToListForHearing().removeIf(
                        caseLink -> caseLink.getValue().getCaseReference().equals(details.getId().toString()));

            if (caseRemovedFromCasesAcceptedToListForHearing) {
                log.info("Submitting {} event for case id: {}", CASEWORKER_REMOVE_CASES_BULK_LIST, details.getId());
                ccdUpdateService.submitBulkActionEvent(
                    bulkCaseDetails.getId(),
                    CASEWORKER_REMOVE_CASES_BULK_LIST,
                    user,
                    serviceAuthorization
                );
            }
        }

        details.getData().setBulkListCaseReferenceLink(null);
    }
}
