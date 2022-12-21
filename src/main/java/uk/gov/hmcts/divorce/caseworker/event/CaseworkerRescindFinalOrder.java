package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentUtil.removeDocumentsBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;

@Component
@Slf4j
public class CaseworkerRescindFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_RESCIND_FINAL_ORDER = "caseworker-rescind-final-order";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CASEWORKER_RESCIND_FINAL_ORDER)
            .forStates(FinalOrderComplete, GeneralConsiderationComplete)
            .name("Rescind final order")
            .description("Rescind final order")
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR, CASE_WORKER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Rescind final order about to submit callback invoked for case id: {}", details.getId());
        CaseData caseData = details.getData();

        // Remove documents
        removeDocumentsBasedOnContactPrivacy(caseData,
            List.of(FINAL_ORDER_GRANTED, FINAL_ORDER_GRANTED_COVER_LETTER_APP_1, FINAL_ORDER_GRANTED_COVER_LETTER_APP_2));

        // Set audit
        FinalOrder finalOrderForAudit = FinalOrder.builder()
            .rescindedDateTime(LocalDateTime.now(clock))
            .grantedDate(caseData.getFinalOrder().getGrantedDate())
            .build();

        caseData.setRescindedFinalOrders(CaseData.addAuditRecord(caseData.getRescindedFinalOrders(), finalOrderForAudit));

        // Nullify current final order
        caseData.setFinalOrder(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
