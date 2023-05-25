package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_NOT_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerAmendApplicationType implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_AMEND_APPLICATION_TYPE = "caseworker-amend-application-type";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CASEWORKER_AMEND_APPLICATION_TYPE)
            .forStates(STATES_NOT_WITHDRAWN_OR_REJECTED)
            .name("Amend application type")
            .description("Amend application type")
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR, CASE_WORKER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker Amend Application Type about to submit callback invoked for case id: {}", details.getId());
        CaseData caseData = details.getData();

        List<String> validationErrors = new ArrayList<>();

        if (caseData.getDivorceOrDissolution() == null) {
            validationErrors.add("divorceOrDissolution is null");
        } else if (caseData.isJudicialSeparationCase()) {
            if (caseData.isDivorce()) {
                caseData.setSupplementaryCaseType(SEPARATION);
            } else {
                caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
            }
        } else {
            if (caseData.isDivorce()) {
                caseData.setDivorceOrDissolution(DISSOLUTION);
            } else {
                caseData.setDivorceOrDissolution(DIVORCE);
            }
        }
        caseData.getLabelContent().setUnionType(caseData.getDivorceOrDissolution());

        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(validationErrors)
                .build();
        } else {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }
    }
}
