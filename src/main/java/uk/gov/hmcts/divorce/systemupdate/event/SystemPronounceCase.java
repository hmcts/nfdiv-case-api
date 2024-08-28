package uk.gov.hmcts.divorce.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.task.UpdateCaseCourtHearingProvider;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.RemoveExistingConditionalOrderPronouncedDocument;

import java.util.EnumSet;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.SeparationOrderGranted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemPronounceCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PRONOUNCE_CASE = "system-pronounce-case";
    private final ConditionalOrderPronouncedNotification conditionalOrderPronouncedNotification;
    private final GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;
    private final RemoveExistingConditionalOrderPronouncedDocument removeExistingConditionalOrderPronouncedDocument;
    private final NotificationDispatcher notificationDispatcher;
    private final CcdSearchService ccdSearchService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(
            configBuilder
                .event(SYSTEM_PRONOUNCE_CASE)
                .forStates(EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived, ConditionalOrderPronounced))
                .name("System pronounce case")
                .description("System pronounce case")
                .grant(CREATE_READ_UPDATE, SYSTEMUPDATE, SUPER_USER)
                .grantHistoryOnly(SOLICITOR, CASE_WORKER, LEGAL_ADVISOR, JUDGE)
                .aboutToSubmitCallback(this::aboutToSubmit)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        if (isEmpty(caseData.getConditionalOrder().getCourt())) {
            updateMissingFields(caseData);
        }

        log.info("Conditional order pronounced for Case({})", caseId);

        generateConditionalOrderGrantedDocs(details, beforeDetails);
        notificationDispatcher.send(conditionalOrderPronouncedNotification, caseData, details.getId());

        final State state = caseData.isJudicialSeparationCase() ? SeparationOrderGranted : ConditionalOrderPronounced;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(state)
            .data(caseData)
            .build();
    }

    private void generateConditionalOrderGrantedDocs(final CaseDetails<CaseData, State> details,
                                                     final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData newCaseData = details.getData();

        if (newCaseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent()) {
            ConditionalOrder oldCO = beforeDetails.getData().getConditionalOrder();
            ConditionalOrder newCO = newCaseData.getConditionalOrder();

            if (!newCO.getPronouncementJudge().equals(oldCO.getPronouncementJudge())
                || !newCO.getCourt().equals(oldCO.getCourt())
                || !newCO.getDateAndTimeOfHearing().equals(oldCO.getDateAndTimeOfHearing())) {

                caseTasks(
                    removeExistingConditionalOrderPronouncedDocument,
                    generateConditionalOrderPronouncedDocument
                ).run(details);
            }

        } else {
            caseTasks(generateConditionalOrderPronouncedDocument).run(details);
        }
    }

    private void updateMissingFields(CaseData caseData) {

        final var user = idamService.retrieveSystemUpdateUserDetails();
        final var serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                    .must(matchQuery("data.bulkListCaseReferenceLink.CaseReference", caseData.getBulkListCaseReferenceLink().getCaseReference()))
                    .must(existsQuery("data.coCourt"));

            Map<String, Object> otherCaseData =
                    ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth).stream().findFirst().orElseThrow().getData();

            CaseData convertedCaseData = objectMapper.convertValue(otherCaseData, CaseData.class);

            ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

            conditionalOrder.setCourt(convertedCaseData.getConditionalOrder().getCourt());
            conditionalOrder.setDateAndTimeOfHearing(convertedCaseData.getConditionalOrder().getDateAndTimeOfHearing());
            conditionalOrder.setOfflineCertificateOfEntitlementDocumentSentToApplicant1(NO);
            conditionalOrder.setOfflineCertificateOfEntitlementDocumentSentToApplicant2(NO);

        } catch (final CcdSearchCaseException e) {
            log.error("Error returned", e);
        } catch (final CcdConflictException e) {
            log.info("Error returned");
        }
    }
}
