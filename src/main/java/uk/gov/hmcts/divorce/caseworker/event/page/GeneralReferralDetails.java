package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.Map;

import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.generalApplicationLabels;

public class GeneralReferralDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("generalReferral", this::midEvent)
            .pageLabel("General Referral")
            .complex(CaseData::getGeneralReferral)
                .mandatory(GeneralReferral::getGeneralReferralReason)
                .mandatory(
                    GeneralReferral::getSelectedGeneralApplication,
                    "generalReferralReason=\"generalApplicationReferral\""
                )
                .mandatory(GeneralReferral::getGeneralReferralUrgentCase)
                .mandatory(GeneralReferral::getGeneralReferralUrgentCaseReason, "generalReferralUrgentCase=\"Yes\"")
                .mandatory(GeneralReferral::getGeneralReferralFraudCase)
                .mandatory(GeneralReferral::getGeneralReferralFraudCaseReason, "generalReferralFraudCase=\"Yes\"")
                .mandatory(GeneralReferral::getGeneralApplicationFrom, "generalReferralReason=\"generalApplicationReferral\"")
                .optional(GeneralReferral::getGeneralApplicationReferralDate)
                .mandatory(GeneralReferral::getGeneralReferralType)
                .mandatory(GeneralReferral::getAlternativeServiceMedium, "generalReferralType=\"alternativeServiceApplication\"")
                .mandatory(GeneralReferral::getGeneralReferralJudgeOrLegalAdvisorDetails)
                .mandatory(GeneralReferral::getGeneralReferralFeeRequired)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData data = details.getData();
        GeneralApplication generalApplication = getSelectedGeneralApplication(data);
        if (generalApplication != null) {
            data.getGeneralReferral().setGeneralReferralDocuments(generalApplication.getGeneralApplicationDocuments());
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private GeneralApplication getSelectedGeneralApplication(CaseData data) {
        var referredApplication = data.getGeneralReferral().getSelectedGeneralApplication();
        if (referredApplication == null) {
            return null;
        }

        for (Map.Entry<Integer, String> entry : generalApplicationLabels(data).entrySet()) {
            String applicationLabel = entry.getValue();
            String referredApplicationLabel = referredApplication.getValueLabel();

            if (referredApplicationLabel.equals(applicationLabel)) {
                int applicationIdx = entry.getKey();
                return data.getGeneralApplications().get(applicationIdx).getValue();
            }
        }
        return null;
    }
}
