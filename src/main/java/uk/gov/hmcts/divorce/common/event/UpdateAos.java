package uk.gov.hmcts.divorce.common.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.Applicant2HowToRespondToApplication;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolAosJurisdiction;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolAosOtherProceedings;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolUpdateAosApplicant1Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class UpdateAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String UPDATE_AOS = "update-aos";

    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    private final List<CcdPageConfiguration> pages = asList(
        new Applicant2SolUpdateAosApplicant1Application(),
        new Applicant2HowToRespondToApplication(),
        new Applicant2SolAosJurisdiction(),
        new Applicant2SolAosOtherProceedings()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(UPDATE_AOS)
            .forState(AosDrafted)
            .name("Update AoS")
            .description("Update Acknowledgement of Service")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .endButtonLabel("Save Updated AoS Response")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR, APPLICANT_2)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(addMiniApplicationLink
                .apply(details)
                .getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        if (data.getAcknowledgementOfService().getConfirmDisputeApplication() == YesOrNo.NO
            && data.getAcknowledgementOfService().getHowToRespondApplication() == HowToRespondApplication.DISPUTE_DIVORCE) {

            data.getAcknowledgementOfService().setHowToRespondApplication(null);
            data.getAcknowledgementOfService().setConfirmDisputeApplication(null);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
