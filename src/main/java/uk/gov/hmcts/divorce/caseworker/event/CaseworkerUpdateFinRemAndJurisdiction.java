package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateMarriageDate;

@Slf4j
@Component
public class CaseworkerUpdateFinRemAndJurisdiction implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION = "caseworker-update-fin-rem-and-jurisdiction";

    private static final String JOINT_APPLICATION = "applicationType=\"jointApplication\"";

    @Autowired
    private GenerateApplication generateApplication;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION)
            .forAllStates()
            .name("Update FinRem and Jurisdiction")
            .description("Update FinRem and Jurisdiction")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                SUPER_USER)
            .grantHistoryOnly(
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("updateFinRemAndJurisdiction")
            .complex(CaseData::getApplication)
            .label("Label-CorrectJurisdictionDetails", "### Jurisdiction connection details")
            .complex(Application::getJurisdiction)
            .mandatory(Jurisdiction::getConnections)
            .done()
            .done()
            .mandatory(CaseData::getDivorceOrDissolution, "jurisdictionConnections=\"NEVER_SHOW\"")
            .mandatory(CaseData::getApplicationType, NEVER_SHOW)
            .complex(CaseData::getLabelContent)
            .readonlyNoSummary(LabelContent::getTheApplicant2, NEVER_SHOW)
            .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, NEVER_SHOW)
            .readonlyNoSummary(LabelContent::getRespondentsOrApplicant2s, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplicant1)
            .label("Label-CorrectApplicant1FODetails",
                "### ${labelContentApplicantsOrApplicant1s} financial order details")
            .mandatory(Applicant::getFinancialOrder)
            .mandatory(Applicant::getFinancialOrdersFor, "applicant1FinancialOrder=\"Yes\"")
            .done()
            .complex(CaseData::getApplicant2, JOINT_APPLICATION)
            .label("Label-CorrectApplicant2FODetails",
                "### ${labelContentRespondentsOrApplicant2s} financial order details", JOINT_APPLICATION)
            .mandatory(Applicant::getFinancialOrder, JOINT_APPLICATION, null,
                "Does ${labelContentTheApplicant2} wish to apply for a financial order?")
            .mandatory(Applicant::getFinancialOrdersFor, "applicant2FinancialOrder=\"Yes\" AND applicationType=\"jointApplication\"")
            .done()
            .done()
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION, details.getId());
        CaseData caseData = details.getData();

        if (null != caseData.getApplication().getIssueDate()) {
            final CaseDetails<CaseData, State> result = caseTasks(generateApplication).run(details);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(result.getData())
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
