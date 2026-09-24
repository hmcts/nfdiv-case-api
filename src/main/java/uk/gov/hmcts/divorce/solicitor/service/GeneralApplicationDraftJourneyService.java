package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

@Service
@RequiredArgsConstructor
public class GeneralApplicationDraftJourneyService {

    private final GeneralApplicationTypeOptionsService generalApplicationTypeOptionsService;
    private final GeneralApplicationDraftSubmissionService generalApplicationDraftSubmissionService;

    public void prepareAboutToStart(CaseDetails<CaseData, State> details, Applicant applicant) {
        CaseData data = details.getData();
        data.setGeneralApplication(GeneralApplication.builder().build());

        InterimApplicationOptions interim = applicant.getInterimApplicationOptions();
        GeneralApplicationD11JourneyOptions journey = interim.getGeneralApplicationD11JourneyOptions();

        DynamicList existing = journey.getSolType();
        DynamicList options = generalApplicationTypeOptionsService.buildOptions(details.getState(), data, existing);
        journey.setSolType(options);
    }

    public void prepareAboutToSubmit(CaseDetails<CaseData, State> details, Applicant applicant) {
        generalApplicationDraftSubmissionService.buildGeneralApplication(
            details,
            details.getData(),
            applicant
        );
    }
}
