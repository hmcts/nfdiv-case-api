package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolStatementOfTruth;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitAosService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SolicitorSubmitAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT_AOS = "solicitor-submit-aos";

    @Autowired
    private SolicitorSubmitAosService solicitorSubmitAosService;

    private final List<CcdPageConfiguration> pages = List.of(
        new Applicant2SolStatementOfTruth()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final var caseData = details.getData();
        final var acknowledgementOfService = caseData.getAcknowledgementOfService();

        final List<String> errors = validateAos(acknowledgementOfService);

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(errors)
                .build();
        }

        final CaseDetails<CaseData, State> updateDetails = solicitorSubmitAosService.submitAos(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updateDetails.getData())
            .state(updateDetails.getState())
            .build();
    }

    private List<String> validateAos(final AcknowledgementOfService acknowledgementOfService) {

        final List<String> errors = new ArrayList<>();

        if (NO.equals(acknowledgementOfService.getStatementOfTruth())) {
            errors.add("You must be authorised by the respondent to sign this statement.");
        }

        if (NO.equals(acknowledgementOfService.getPrayerHasBeenGiven())) {
            errors.add("The respondent must have given their prayer.");
        }

        if (NO.equals(acknowledgementOfService.getConfirmReadPetition())) {
            errors.add("The respondent must have read the application for divorce.");
        }

        return errors;
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(SOLICITOR_SUBMIT_AOS)
            .forStateTransition(AosDrafted, Holding)
            .name("Submit AoS")
            .description("Submit AoS")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grant(READ,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU,
                CASEWORKER_LEGAL_ADVISOR,
                CASEWORKER_SUPERUSER));
    }
}
