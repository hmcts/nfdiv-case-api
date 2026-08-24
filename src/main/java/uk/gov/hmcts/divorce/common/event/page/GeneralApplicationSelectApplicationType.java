package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;
import java.util.Optional;

public class GeneralApplicationSelectApplicationType implements CcdPageConfiguration {

    private static final String GENERAL_APPLICATION_SOL_GUIDE = "Refer to the <a href=\"https://www.gov.uk/government/publications/myhmcts"
            + "-how-to-make-follow-up-applications-for-a-divorce-or-dissolution/general-applications-alternative-service-and-deemed-"
            + "and-dispensed\" target=\"_blank\" rel=\"noopener noreferrer\">Solicitor Guidance</a>";
    private static final String SERVICE_APPLICATION_TYPE_NOT_ALLOWED_ERROR = "The selected application type is a Service Application and "
        + "cannot be processed through the General application event. Please use the appropriate Service Application event to continue.";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationSelectType", this::midEvent)
            .pageLabel("Select Application Type")
            .complex(CaseData::getGeneralApplication)
                .mandatory(GeneralApplication::getGeneralApplicationType)
                .mandatory(GeneralApplication::getGeneralApplicationTypeOtherComments,
                    "generalApplicationType=\"other\"")
                .mandatory(GeneralApplication::getGeneralApplicationUrgentCase)
                .mandatory(GeneralApplication::getGeneralApplicationUrgentCaseReason, "generalApplicationUrgentCase=\"Yes\"")
                .done()
            .label("generalApplicationSolGuide", GENERAL_APPLICATION_SOL_GUIDE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final GeneralApplicationType applicationType = Optional.ofNullable(caseData.getGeneralApplication())
            .map(GeneralApplication::getGeneralApplicationType)
            .orElse(null);

        final boolean isServiceApplicationType =
            GeneralApplicationType.DISPENSED_WITH_SERVICE.equals(applicationType)
                || GeneralApplicationType.DEEMED_SERVICE.equals(applicationType)
                || GeneralApplicationType.OTHER_ALTERNATIVE_SERVICE_METHODS.equals(applicationType);

        if (isServiceApplicationType) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(SERVICE_APPLICATION_TYPE_NOT_ALLOWED_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(null)
            .build();
    }
}
