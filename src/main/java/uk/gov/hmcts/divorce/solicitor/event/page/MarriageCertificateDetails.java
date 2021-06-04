package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.MarriageDetails;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class MarriageCertificateDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("MarriageCertificateDetails")
            .pageLabel("Marriage certificate details")
            .label(
                "LabelNFDBanner-MarriageCertificateDetails",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-MarriageCertificateDetails",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getDate)
                .mandatory(MarriageDetails::getApplicant1Name)
                .mandatory(MarriageDetails::getApplicant2Name)
                .done()
            .complex(CaseData::getMarriageDetails)
                .mandatory(
                    MarriageDetails::getMarriedInUk, null, null,
                    "Did the marriage take place in the UK?"
                )
                .mandatory(
                    MarriageDetails::getPlaceOfMarriage,
                    "marriageMarriedInUk=\"No\""
                )
                .mandatory(
                    MarriageDetails::getCountryOfMarriage,
                    "marriageMarriedInUk=\"No\""
                )
            .done();
    }
}
