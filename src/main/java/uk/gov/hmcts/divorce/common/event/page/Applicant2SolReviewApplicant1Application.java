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
                .readonlyWithLabel(Application::getMiniApplicationLink, "View application:")
                .done()
            .label("LabelRespSol-DueDate", "The response is due by ${dueDate}")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getConfirmReadPetition)
            .label("LabelRespSol-AOSConfirmRead",
                "${labelContentTheApplicant2UC} must have read the application before you can respond.",
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
