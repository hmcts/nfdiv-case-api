package uk.gov.hmcts.divorce.common.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Applicant2SolReviewApplicant1Application implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolReviewApplicant1Application", this::midEvent)
            .pageLabel("Review application")
            .complex(CaseData::getApplication)
                .readonly(Application::getMiniApplicationLink)
                .done()
            .label("LabelRespSol-AOSRespond",
                "### Respond to ${labelContentDivorceOrCivilPartnershipApplication}\n\n"
                    + "# Reference number\n"
                    + "${[CASE_REFERENCE]}\n\n"
                    + "# ${labelContentApplicant2UC}\n"
                    + "${applicant2FirstName} ${applicant2LastName}\n\n"
                    + "You must respond to this application within 14 days of ${labelContentTheApplicant2} receiving the letter "
                    + "from the courts."
                    + " If you don't, the applicant can ask the court to move the ${labelContentDivorceOrCivilPartnership} "
                    + "forwards without ${labelContentTheApplicant2}. "
                    + "${labelContentTheApplicant2UC} may have to pay extra fees if this happens.\n\n")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getConfirmReadPetition)
            .label("LabelRespSol-AOSConfirmRead",
                "### ${labelContentTheApplicant2} has not read the application\n\n"
                    + "${labelContentTheApplicant2} must have read the application in order to respond.",
              "confirmReadPetition=\"No\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for Applicant2SolReviewApplicant1Application");

        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        AcknowledgementOfService acknowledgementOfService = data.getAcknowledgementOfService();

        if (!acknowledgementOfService.getConfirmReadPetition().toBoolean()) {
            String labelContent = data.getLabelContent().getTheApplicant2();
            errors.add("To continue," + labelContent + " must have read the application in order to respond");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
