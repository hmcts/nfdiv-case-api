package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Application;
import uk.gov.hmcts.divorce.common.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolPayAccount implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolPayAccount")
            .pageLabel("Pay account")
            .showCondition("solPaymentHowToPay=\"feePayByAccount\"")
            .label(
                "LabelNFDBanner-SolPayAccount",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-SolPayAccount",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .complex(CaseData::getApplication)
                .optional(Application::getPbaNumbers)
                .mandatory(Application::getFeeAccountReference)
                .done();
    }
}
