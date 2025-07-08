package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.DeemedServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Service
@RequiredArgsConstructor
public class InterimApplicationSubmissionService {
    private final DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;
    private final NotificationDispatcher notificationDispatcher;
    private final DeemedServiceApplicationSubmittedNotification deemedApplicationSubmittedNotification;

    public DivorceDocument generateAnswerDocument(
        long caseId,
        Applicant applicant,
        CaseData caseData
    ) {
        InterimApplicationType applicationType = applicant.getInterimApplicationOptions().getInterimApplicationType();

        if (InterimApplicationType.DEEMED_SERVICE.equals(applicationType)) {
            return deemedServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
        }

        if (InterimApplicationType.ALTERNATIVE_SERVICE.equals(applicationType)) {
            return DivorceDocument.builder().build();
        }

        throw new UnsupportedOperationException();
    }

    public void sendNotifications(
        long caseId,
        AlternativeServiceType serviceType,
        CaseData caseData
    ) {
        if (AlternativeServiceType.DEEMED.equals(serviceType)) {
            notificationDispatcher.send(deemedApplicationSubmittedNotification, caseData, caseId);
            return;
        }

        if (AlternativeServiceType.ALTERNATIVE_SERVICE.equals(serviceType)) {
            return;
        }

        throw new UnsupportedOperationException();
    }
}
