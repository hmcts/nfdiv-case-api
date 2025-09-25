package uk.gov.hmcts.divorce.citizen.event;

import com.google.common.base.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenStartInterimApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_START_INTERIM_APPLICATION = "citizen-start-interim-application";

    private final DocumentRemovalService documentRemovalService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CITIZEN_START_INTERIM_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .showCondition(NEVER_SHOW)
            .name("Start Interim Application")
            .description("Citizen Start Interim Application")
            .grant(CREATE_READ_UPDATE, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        long caseId = details.getId();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_START_INTERIM_APPLICATION, caseId);

        final Applicant afterApplicant = details.getData().getApplicant1();
        final Applicant beforeApplicant = beforeDetails.getData().getApplicant1();

        if (interimApplicationTypeHasChanged(afterApplicant, beforeApplicant)) {
            resetApplicationOptions(afterApplicant);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    private boolean interimApplicationTypeHasChanged(Applicant afterApplicant, Applicant beforeApplicant) {
        final InterimApplicationOptions beforeOptions = beforeApplicant.getInterimApplicationOptions();
        final InterimApplicationOptions afterOptions = afterApplicant.getInterimApplicationOptions();

        return beforeOptions != null && !Objects.equal(
            beforeOptions.getInterimApplicationType(),
            afterOptions.getInterimApplicationType()
        );
    }

    private void resetApplicationOptions(Applicant applicant) {
        final InterimApplicationOptions options = applicant.getInterimApplicationOptions();

        if (!CollectionUtils.isEmpty(options.getInterimAppsEvidenceDocs())) {
            documentRemovalService.deleteDocument(options.getInterimAppsEvidenceDocs());
        }

        applicant.setInterimApplicationOptions(
            options.toBuilder()
                .interimAppsUseHelpWithFees(null)
                .interimAppsHwfRefNumber(null)
                .interimAppsHaveHwfReference(null)
                .interimAppsCanUploadEvidence(null)
                .interimAppsCannotUploadDocs(null)
                .interimAppsEvidenceDocs(null)
                .build()
        );
    }
}
