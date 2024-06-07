package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.common.event.RegenerateApplication.REGENERATE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class CaseworkerUpdateFinRemAndJurisdictionJoint implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION_JOINT = "caseworker-update-fin-rem-and-jurisd-joint";

    private static final String NEVER_SHOW = "jurisdictionConnections=\"NEVER_SHOW\"";

    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION_JOINT)
            .forAllStates()
            .name("Update FinRem and Jurisdiction")
            .showCondition("applicationType=\"jointApplication\"")
            .description("Update FinRem and Jurisdiction")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                SUPER_USER)
            .grantHistoryOnly(
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE)
            .submittedCallback(this::submitted))
            .page("updateFinRemAndJurisdiction")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getTheApplicant2, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplication)
                .label("Label-CorrectJurisdictionDetails", "### Jurisdiction connection details")
                .complex(Application::getJurisdiction)
                    .mandatory(Jurisdiction::getConnections)
                    .done()
                .done()
            .complex(CaseData::getApplicant1)
                .label("Label-CorrectApplicant1FODetails",
                    "### ${labelContentApplicantsOrApplicant1s} financial order details")
                .mandatory(Applicant::getFinancialOrder)
                .mandatory(Applicant::getFinancialOrdersFor, "applicant1FinancialOrder=\"Yes\"")
                .done()
            .complex(CaseData::getApplicant2)
                .label("Label-CorrectApplicant2FODetails",
                    "### ${labelContentRespondentsOrApplicant2s} financial order details")
                .mandatoryWithLabel(Applicant::getFinancialOrder,
                    "Does ${labelContentTheApplicant2} wish to apply for a financial order?")
                .mandatory(Applicant::getFinancialOrdersFor, "applicant2FinancialOrder=\"Yes\"")
                .done()
            .done();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for case id: {}", CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION_JOINT, details.getId());

        if (null != details.getData().getApplication().getIssueDate()) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            ccdUpdateService
                .submitEvent(details.getId(), REGENERATE_APPLICATION, user, serviceAuth);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
