package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class SolicitorAlternativeServiceAbleToUploadEvidence implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("altServiceAbleToUploadEvidence")
            .pageLabel("Alternative Service App")
            .complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .complex(InterimApplicationOptions::getAlternativeServiceJourneyOptions)
            .mandatory(AlternativeServiceJourneyOptions::getSolAltServiceCanUploadEvidence)
            .mandatory(AlternativeServiceJourneyOptions::getSolAltServiceSuccessfulSendReason,
                "applicant1SolAltServiceCanUploadEvidence=\"Yes\"")
            .mandatory(AlternativeServiceJourneyOptions::getSolAltServiceWhySendThisWay,
                "applicant1SolAltServiceCanUploadEvidence=\"No\"")
            .done();
    }
}
