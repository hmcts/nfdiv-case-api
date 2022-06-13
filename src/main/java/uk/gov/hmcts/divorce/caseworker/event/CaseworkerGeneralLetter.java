package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.GeneralLetterService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerGeneralLetter implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CREATE_GENERAL_LETTER = "caseworker-create-general-letter";
    private static final String CREATE_GENERAL_LETTER_TITLE = "Create general letter";

    @Autowired
    private GeneralLetterService generalLetterService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CREATE_GENERAL_LETTER)
            .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
            .name(CREATE_GENERAL_LETTER_TITLE)
            .description(CREATE_GENERAL_LETTER_TITLE)
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN))
            .page("createGeneralLetter")
            .pageLabel(CREATE_GENERAL_LETTER_TITLE)
            .complex(CaseData::getGeneralLetter)
                .mandatory(GeneralLetter::getGeneralLetterParties)
                .mandatory(GeneralLetter::getOtherRecipientName, "generalLetterParties=\"other\"")
                .mandatory(GeneralLetter::getOtherRecipientAddress, "generalLetterParties=\"other\"")
                .mandatory(GeneralLetter::getGeneralLetterDetails)
                .optional(GeneralLetter::getGeneralLetterAttachments)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker create general letter about to submit callback invoked for Case Id: {}", details.getId());

        generalLetterService.processGeneralLetter(details);

        //clear general letter field so that on next general letter old data is not shown
        details.getData().setGeneralLetter(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
