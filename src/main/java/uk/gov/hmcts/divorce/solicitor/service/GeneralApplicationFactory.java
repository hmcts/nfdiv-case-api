package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GeneralApplicationFactory {

    private final CitizenGeneralApplicationSubmissionService submissionService;

    private final Clock clock;

    public GeneralApplication createFromJourneyOptions(InterimApplicationOptions journeyOptions, boolean isApplicant1,
                                                       ApplicationType applicationType) {
        return GeneralApplication.builder()
            .generalApplicationParty(GeneralParties.from(isApplicant1, applicationType))
            .generalApplicationReceivedDate(LocalDateTime.now(clock))
            .generalApplicationType(mapByLabel(journeyOptions.getGeneralApplicationD11JourneyOptions().getSolType()))
            .generalApplicationOtherTypeDetails(journeyOptions.getOtherGeneralApplicationTypeDetails())
            .generalApplicationSubmittedOnline(YesOrNo.YES)
            .generalApplicationDocuments(submissionService.collectSupportingDocuments(journeyOptions))
            .build();
    }

    private GeneralApplicationType mapByLabel(DynamicList solType) {
        if (solType != null && solType.getValue() != null && solType.getValue().getLabel() != null) {
            String label = solType.getValue().getLabel();
            for (GeneralApplicationType t : GeneralApplicationType.values()) {
                if (t.getLabel().equals(label)) {
                    return t;
                }
            }
        }
        return null;
    }
}
