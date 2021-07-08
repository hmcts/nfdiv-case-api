package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolPayment implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolPayment")
            .pageLabel("Payment")
            .label(
                "LabelNFDBanner-SolPayment",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-SolPayment",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label(
                "LabelSolPaymentPara-1",
                "Amount to pay: **£${solApplicationFeeInPounds}**")
            .complex(CaseData::getApplication)
                .mandatory(Application::getSolPaymentHowToPay)
                .done();
    }
}
