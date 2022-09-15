package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrderCoverLetter;
import uk.gov.hmcts.divorce.caseworker.service.task.SendFinalOrderGrantedNotifications;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;

@Slf4j
@Component
public class CaseworkerGrantFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_GRANT_FINAL_ORDER = "caseworker-grant-final-order";

    @Autowired
    private Clock clock;

    @Autowired
    private GenerateFinalOrder generateFinalOrder;

    @Autowired
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Autowired
    private SendFinalOrderGrantedNotifications sendFinalOrderGrantedNotifications;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_GRANT_FINAL_ORDER)
            .forStateTransition(FinalOrderRequested, FinalOrderComplete)
            .name("Grant Final order")
            .description("Grant Final order")
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Submit")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR))
            .page("grantFinalOrder")
            .pageLabel("Grant Final Order")
            .complex(CaseData::getFinalOrder)
            .mandatory(FinalOrder::getGranted)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_GRANT_FINAL_ORDER, details.getId());

        CaseData caseData = details.getData();

        LocalDate dateFinalOrderEligibleFrom = caseData.getFinalOrder().getDateFinalOrderEligibleFrom();

        LocalDateTime currentDateTime = LocalDateTime.now(clock);

        if (dateFinalOrderEligibleFrom != null
            && dateFinalOrderEligibleFrom.isAfter(currentDateTime.toLocalDate())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList("Case is not yet eligible for Final Order"))
                .build();
        }

        caseData.getFinalOrder().setGrantedDate(currentDateTime);

        Long caseId = details.getId();

        if (caseData.getApplicant1().isOffline()) {
            log.info("Generating final order cover letter for Applicant 1 for case id: {} ", caseId);
            generateFinalOrderCoverLetter.apply(
                caseData,
                caseId,
                caseData.getApplicant1(),
                FINAL_ORDER_GRANTED_COVER_LETTER_APP_1
            );
        }

        if (isBlank(caseData.getApplicant2EmailAddress()) || caseData.getApplicant2().isOffline()) {
            log.info("Generating final order cover letter for Applicant 2 for case id: {} ", caseId);
            generateFinalOrderCoverLetter.apply(
                caseData,
                caseId,
                caseData.getApplicant2(),
                FINAL_ORDER_GRANTED_COVER_LETTER_APP_2
            );
        }

        generateFinalOrder.apply(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("CitizenSaveAndClose submitted callback invoked for case id: {}", details.getId());

        sendFinalOrderGrantedNotifications.apply(details);

        return SubmittedCallbackResponse.builder().build();
    }
}
