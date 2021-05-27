package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

@Slf4j
@Component
public class ValidateHwfReference implements CCDConfig<CaseData, State, UserRole> {

    public static final String VALIDATE_HWF_CODE = "validate-hwf-code";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(VALIDATE_HWF_CODE)
            .forStates(AwaitingHWFDecision)
            .name("Validate HWF Code")
            .description("Validate HWF Code")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .displayOrder(1)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN)
            .grant(READ, CASEWORKER_DIVORCE_SOLICITOR, CASEWORKER_DIVORCE_SUPERUSER, CASEWORKER_DIVORCE_COURTADMIN_LA))
            .page("hwfCodeValidationResults")
            .pageLabel("HWF Results")
            .mandatory(CaseData::getHwfCodeValidForFullAmount)
            .mandatory(CaseData::getHwfAmountOutstanding, "hwfCodeValidForFullAmount=\"No\"");
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> caseDetails,
        CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Validate HWF Code about to submit callback invoked for case Id {}", caseDetails.getId());

        CaseData data = caseDetails.getData();

        State caseState = caseDetails.getState();

        if (!AwaitingHWFDecision.equals(caseState)) {
            log.info("Event Validate HWF Code with case state {} for case Id {} is invalid", caseState, caseDetails.getId());
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(singletonList("Case State is not valid for event Validate HWF Code"))
                .build();
        }

        boolean isHwfCodeValidForFullAmount = data.getHwfCodeValidForFullAmount().toBoolean();

        State endState;

        if (isHwfCodeValidForFullAmount) {
            log.info("For Case Id {} HWF code is valid for full amount", caseDetails.getId());
            endState = isEmpty(data.getDocumentsUploaded()) ? AwaitingDocuments : Submitted;
        } else {
            endState = AwaitingPayment;
        }
        log.info("For Case Id {} Moving case endState from {} to {} ", caseDetails.getId(), caseState, endState);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(endState)
            .build();
    }
}
