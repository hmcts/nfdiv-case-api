package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerAddTranslation implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REQUEST_TRANSLATION_WLU = "caseworker-add-translation";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REQUEST_TRANSLATION_WLU)
            .forStates(POST_SUBMISSION_STATES)
            .name("Add translation")
            .description("Add translation")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SYSTEMUPDATE))
            .page("addTranslationApplicant")
            .pageLabel("Applicant/Applicant1 details")
            .complex(CaseData:: getApplicant1)
                .readonlyNoSummary(Applicant::getLegalProceedingsDetails)
                .optional(Applicant::getLegalProceedingsDetailsTranslated)
                .optional(Applicant::getLegalProceedingsDetailsTranslatedTo)
            .done()
            .label("legalProceedingsAppLineSep","<hr>")
            .page("addTranslationRespondent")
            .pageLabel("Respondent/Applicant2 details")
            .complex(CaseData:: getApplicant2)
                .readonlyNoSummary(Applicant::getLegalProceedingsDetails)
                .optional(Applicant::getLegalProceedingsDetailsTranslated)
                .optional(Applicant::getLegalProceedingsDetailsTranslatedTo)
            .done()
            .label("legalProceedingsRespLineSep","<hr>")
            .page("addTranslationConditionalOrder")
            .pageLabel("Conditional Order details")
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                    .readonlyNoSummary(ConditionalOrderQuestions::getReasonInformationNotCorrect)
                    .optional(ConditionalOrderQuestions::getConfirmInformationStillCorrectTranslated)
                    .optional(ConditionalOrderQuestions::getConfirmInformationStillCorrectTranslatedTo)
                .done()
                .complex(ConditionalOrder::getConditionalOrderApplicant2Questions,"applicationType=\"jointApplication\"")
                    .readonlyNoSummary(ConditionalOrderQuestions::getReasonInformationNotCorrect)
                    .optional(ConditionalOrderQuestions::getConfirmInformationStillCorrectTranslated)
                    .optional(ConditionalOrderQuestions::getConfirmInformationStillCorrectTranslatedTo)
                .done()
            .done()
            .label("conditionalOrderLineSep","<hr>")
            .page("addTranslationOutcomeOfCO")
            .pageLabel("Outcome of Conditional Order");

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker request translation from WLU callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        caseData.getApplication().setWelshPreviousState(details.getState());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(WelshTranslationRequested)
            .build();
    }
}
