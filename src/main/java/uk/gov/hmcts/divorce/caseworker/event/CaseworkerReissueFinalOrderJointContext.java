package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;

import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.*;
import uk.gov.hmcts.divorce.document.DocumentGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.*;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerReissueFinalOrderJointContext implements CCDConfig<CaseData, State, UserRole> {

    public static final String EVENT_ID = "caseworker-reissue-final-order";
    private static final String REISSUE_LABEL = "Reissue FO (Joint Context)";
    private static final String ERROR_MISSING_PARTY_DATA = "Original joint party details are missing. Enter values before continuing.";

    private final DocumentGenerator documentGenerator;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(EVENT_ID)
            .forState(FinalOrderComplete)
            .showCondition("finalOrderSwitchedToSole=\"Yes\"")
            .name(REISSUE_LABEL)
            .description(REISSUE_LABEL)
            .showEventNotes()
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, SOLICITOR))
            .page("reissueFoJoint")
            .pageLabel(REISSUE_LABEL)
            .complex(CaseData::getFinalOrder)
                .complex(FinalOrder::getReissueContext)
                    .optional(FinalOrderReissueContext::getOriginalApplicant1FullNameForReissue)
                    .optional(FinalOrderReissueContext::getOriginalApplicant2FullNameForReissue)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData data = details.getData();

        if (data.getFinalOrder().getReissueContext() == null) {
            data.getFinalOrder().setReissueContext(FinalOrderReissueContext.builder().build());
        }

        // Pre-populate from snapshot if present
        OriginalJointPartySnapshot snapshot = data.getApplication().getOriginalJointPartySnapshot();
        if (snapshot != null) {
            FinalOrderReissueContext context = data.getFinalOrder().getReissueContext();
            if (context.getOriginalApplicationTypeForReissue() == null) {
                context.setOriginalApplicationTypeForReissue(snapshot.getOriginalApplicationType());
            }
            if (context.getOriginalApplicant1FullNameForReissue() == null) {
                context.setOriginalApplicant1FullNameForReissue(snapshot.getOriginalApplicant1FullName());
            }
            if (context.getOriginalApplicant2FullNameForReissue() == null) {
                context.setOriginalApplicant2FullNameForReissue(snapshot.getOriginalApplicant2FullName());
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder().data(data).build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> beforeDetails
    ) {
        CaseData data = details.getData();
        FinalOrderReissueContext ctx = data.getFinalOrder().getReissueContext();

        List<String> errors = new ArrayList<>();

        if (ctx == null || isBlank(ctx.getOriginalApplicant1FullNameForReissue()) || isBlank(ctx.getOriginalApplicant2FullNameForReissue())) {
            errors.add(ERROR_MISSING_PARTY_DATA);
        }
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder().data(data).errors(errors).build();
        }

        ctx.setOriginalApplicationTypeForReissue(JOINT_APPLICATION);

        documentGenerator.generateAndStoreCaseDocument(
            FINAL_ORDER_GRANTED,
            FINAL_ORDER_TEMPLATE_ID,
            FINAL_ORDER_DOCUMENT_NAME,
            data,
            details.getId()
        );
        return AboutToStartOrSubmitResponse.<CaseData, State>builder().data(data).build();
    }
}
