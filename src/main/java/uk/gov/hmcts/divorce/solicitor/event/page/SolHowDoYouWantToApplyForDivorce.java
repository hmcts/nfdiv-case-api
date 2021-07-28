package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

@Slf4j
public class SolHowDoYouWantToApplyForDivorce implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("howDoYouWantToApplyForDivorce", this::midEvent)
            .pageLabel("How do you want to apply for the divorce?")
            .label("LabelNFDBanner-ApplyForDivorce", SOLICITOR_NFD_PREVIEW_BANNER)
            .complex(CaseData::getLabelContent)
                .readonly(LabelContent::getApplicant2, NEVER_SHOW)
                .readonly(LabelContent::getApplicant2UC, NEVER_SHOW)
                .readonly(LabelContent::getTheApplicant2, NEVER_SHOW)
                .readonly(LabelContent::getTheApplicant2UC, NEVER_SHOW)
                .readonly(LabelContent::getUnionType, NEVER_SHOW)
                .readonly(LabelContent::getUnionTypeUC, NEVER_SHOW)
                .done()
            .label("solHowDoYouWantToApplyForDivorcePara-1",
                "The applicant can apply for the divorce on their own (as a 'sole applicant') or with their husband "
                    + "or wife (in a 'joint application').\n\n"
                    + "### Applying as a sole applicant\n\n"
                    + "If the applicant applies as a sole applicant, the applicant's husband or wife responds to the divorce "
                    + "application after you have submitted it.  The applicant will be applying on their own.\n\n"
                    + "### Applying jointly, with the applicant's husband or wife\n\n"
                    + "If the applicant applies jointly, the applicant's husband or wife joins and reviews this online "
                    + "application before it's submitted. They will be applying together.\n\n"
                    + "*How the applicant divides their money and property is dealt with separately. It should not affect "
                    + "the decision on whether to do a sole or a joint application.*")
            .mandatory(CaseData::getApplicationType, null, null,
                "How does the applicant want to apply for the divorce?",
                "Applicant 2 must agree with a joint application in its entirety.")
            .mandatoryWithLabel(CaseData::getDivorceOrDissolution,
                "Is the application for a divorce or dissolution?");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for howDoYouWantToApplyForDivorce");

        final CaseData data = details.getData();
        data.getLabelContent().setApplicationType(data.getApplicationType());
        data.getLabelContent().setUnionType(data.getDivorceOrDissolution());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
