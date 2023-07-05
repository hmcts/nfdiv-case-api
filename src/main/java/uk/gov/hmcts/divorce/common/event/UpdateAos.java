package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class UpdateAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String UPDATE_AOS = "update-aos";

    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        DraftAos.pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(UPDATE_AOS)
            .forStates(AosDrafted, AosOverdue, AwaitingService)
            .name("Update AoS")
            .description("Update Acknowledgement of Service")
            .showCondition("applicationType=\"soleApplication\" AND aosIsDrafted=\"Yes\"")
            .aboutToStartCallback(this::aboutToStart)
            .showSummary()
            .endButtonLabel("Save Updated AoS Response")
            .grant(CREATE_READ_UPDATE, APPLICANT_2, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Update AoS about to start callback invoked for Case Id: {}", details.getId());
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(addMiniApplicationLink
                .apply(details)
                .getData())
            .build();
    }
}
