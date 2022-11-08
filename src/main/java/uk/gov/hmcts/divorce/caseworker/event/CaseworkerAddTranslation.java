package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerAddTranslation implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_ADD_TRANSLATION = "caseworker-add-translation";
    private static final String ALWAYS_HIDE = "applicant1LegalProceedingsDetails=\"ALWAYS_HIDE\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_ADD_TRANSLATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("Add translation")
            .description("Add translation")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SYSTEMUPDATE))
            .page("addTranslationApplicant")
            .pageLabel("Applicant/Applicant1 details")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getApplicantsOrApplicant1s, ALWAYS_HIDE)
            .done()
            .complex(CaseData:: getApplicant1)
                .readonlyNoSummary(Applicant::getLegalProceedingsDetails)
                .optional(Applicant::getLegalProceedingsDetailsTranslated,"applicant1LegalProceedingsDetails=\"*\"")
                .optional(Applicant::getLegalProceedingsDetailsTranslatedTo,"applicant1LegalProceedingsDetails=\"*\"")
            .done()
            .page("addTranslationRespondent")
            .pageLabel("Respondent/Applicant2 details")
            .complex(CaseData:: getApplicant2)
                .readonlyNoSummary(Applicant::getLegalProceedingsDetails)
                .optional(Applicant::getLegalProceedingsDetailsTranslated,"applicant2LegalProceedingsDetails=\"*\"")
                .optional(Applicant::getLegalProceedingsDetailsTranslatedTo,"applicant2LegalProceedingsDetails=\"*\"")
            .done()
            .label("conditionalOrderLineSep","<hr>")
            .complex(CaseData::getAcknowledgementOfService)
                .readonlyNoSummary(AcknowledgementOfService::getReasonCourtsOfEnglandAndWalesHaveNoJurisdiction)
                .optional(AcknowledgementOfService::getReasonCourtsOfEnglandAndWalesHaveNoJurisdictionTranslated,
                    "reasonCourtsOfEnglandAndWalesHaveNoJurisdiction=\"*\"")
                .optional(AcknowledgementOfService::getReasonCourtsOfEnglandAndWalesHaveNoJurisdictionTranslatedTo,
                    "reasonCourtsOfEnglandAndWalesHaveNoJurisdiction=\"*\"")
            .done()
            .page("addTranslationConditionalOrder")
            .pageLabel("Conditional Order details")
            .readonlyNoSummary(CaseData::getApplicationType, "coApplicant1ReasonInformationNotCorrect=\"ALWAYS_HIDE\"")
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                    .readonlyNoSummary(ConditionalOrderQuestions::getReasonInformationNotCorrect)
                    .optional(ConditionalOrderQuestions::getReasonInformationNotCorrectTranslated,
                        "coApplicant1ReasonInformationNotCorrect=\"*\"")
                    .optional(ConditionalOrderQuestions::getReasonInformationNotCorrectTranslatedTo,
                        "coApplicant1ReasonInformationNotCorrect=\"*\"")
                .done()
            .label("conditionalOrderLineSep","<hr>","applicationType=\"jointApplication\"")
                .complex(ConditionalOrder::getConditionalOrderApplicant2Questions,"applicationType=\"jointApplication\"")
                    .readonlyNoSummary(ConditionalOrderQuestions::getReasonInformationNotCorrect)
                    .optional(ConditionalOrderQuestions::getReasonInformationNotCorrectTranslated,
                        "coApplicant2ReasonInformationNotCorrect=\"*\"")
                    .optional(ConditionalOrderQuestions::getReasonInformationNotCorrectTranslatedTo,
                        "coApplicant2ReasonInformationNotCorrect=\"*\"")
                .done()
            .done()
            .page("addTranslationOutcomeOfCO")
            .pageLabel("Outcome of Conditional Order")
            .complex(CaseData::getConditionalOrder)
                .readonlyNoSummary(ConditionalOrder::getRefusalClarificationAdditionalInfo)
                .optional(ConditionalOrder::getRefusalClarificationAdditionalInfoTranslated,
                    "coRefusalClarificationAdditionalInfo=\"*\"")
                .optional(ConditionalOrder::getRefusalClarificationAdditionalInfoTranslatedTo,
                    "coRefusalClarificationAdditionalInfo=\"*\"")
            .done()
            .page("addTranslationFinalOrderOverdue")
            .pageLabel("Final Order Overdue details")
            .complex(CaseData::getFinalOrder)
                .readonlyNoSummary(FinalOrder::getApplicant1FinalOrderLateExplanation)
                .optional(FinalOrder::getApplicant1FinalOrderLateExplanationTranslated,
                    "applicant1FinalOrderLateExplanation=\"*\"")
                .optional(FinalOrder::getApplicant1FinalOrderLateExplanationTranslatedTo,
                    "applicant1FinalOrderLateExplanation=\"*\"")
            .done();
    }
}
