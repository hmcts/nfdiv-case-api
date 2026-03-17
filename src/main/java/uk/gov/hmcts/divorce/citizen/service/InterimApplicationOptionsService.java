package uk.gov.hmcts.divorce.citizen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterimApplicationOptionsService {

    private final DocumentRemovalService documentRemovalService;

    public void resetInterimApplicationOptions(final Applicant applicant) {
        final InterimApplicationOptions options = applicant.getInterimApplicationOptions();

        if (!CollectionUtils.isEmpty(options.getInterimAppsEvidenceDocs())) {
            documentRemovalService.deleteDocument(options.getInterimAppsEvidenceDocs());
        }

        if (options.getGeneralApplicationD11JourneyOptions() != null
            && !CollectionUtils.isEmpty(options.getGeneralApplicationD11JourneyOptions().getPartnerAgreesDocs())) {
            documentRemovalService.deleteDocument(options.getGeneralApplicationD11JourneyOptions().getPartnerAgreesDocs());
        }

        applicant.setInterimApplicationOptions(
            options.toBuilder()
                .interimAppsUseHelpWithFees(null)
                .interimAppsHwfRefNumber(null)
                .interimAppsHaveHwfReference(null)
                .interimAppsCanUploadEvidence(null)
                .interimAppsCannotUploadDocs(null)
                .interimAppsEvidenceDocs(null)
                .generalApplicationD11JourneyOptions(null)
                .build()
        );
    }
}
