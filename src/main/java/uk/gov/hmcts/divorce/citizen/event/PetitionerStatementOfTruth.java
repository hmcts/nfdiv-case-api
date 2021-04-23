package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.validation.service.PetitionValidationService;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.common.model.ValidationResponse;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

@Slf4j
@Component
public class PetitionerStatementOfTruth implements CCDConfig<CaseData, State, UserRole> {

    public static final String PETITIONER_STATEMENT_OF_TRUTH = "petitioner-statement-of-truth";

    @Autowired
    private PetitionValidationService petitionValidationService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(PETITIONER_STATEMENT_OF_TRUTH)
            .forStateTransition(Draft, AwaitingPayment)
            .name("Petitioner Statement of Truth")
            .description("Petitioner confirms SOT")
            .aboutToStartCallback(this::aboutToStart)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grant(READ, CASEWORKER_DIVORCE_SUPERUSER);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Submit petition about to start callback invoked");

        log.info("Retrieving case data");
        final CaseData caseData = details.getData();

        final ValidationResponse validationResponse = petitionValidationService.validateCaseData(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(validationResponse.getErrors())
            .build();
    }
}

