package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.AlternativeServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.BailiffServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.DeemedServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.DispenseServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.InterimApplicationType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.AlternativeServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.BailiffServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.DispenseWithServiceApplicationGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Service
@RequiredArgsConstructor
public class InterimApplicationSubmissionService {
    private final BailiffServiceApplicationGenerator bailiffServiceApplicationGenerator;
    private final NotificationDispatcher notificationDispatcher;

    private final DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;
    private final DeemedServiceApplicationSubmittedNotification deemedApplicationSubmittedNotification;
    private final AlternativeServiceApplicationSubmittedNotification alternativeServiceApplicationSubmittedNotification;
    private final AlternativeServiceApplicationGenerator alternativeServiceApplicationGenerator;
    private final DispenseServiceApplicationSubmittedNotification dispenseServiceApplicationSubmittedNotification;

    private final BailiffServiceApplicationSubmittedNotification bailiffApplicationSubmittedNotification;
    private final DispenseWithServiceApplicationGenerator dispenseWithServiceApplicationGenerator;

    public DivorceDocument generateAnswerDocument(
        long caseId,
        Applicant applicant,
        CaseData caseData
    ) {
        InterimApplicationType applicationType = applicant.getInterimApplicationOptions().getInterimApplicationType();

        if (InterimApplicationType.DEEMED_SERVICE.equals(applicationType)) {
            return deemedServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
        } else if (InterimApplicationType.BAILIFF_SERVICE.equals(applicationType)) {
            return bailiffServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
        } else if (InterimApplicationType.ALTERNATIVE_SERVICE.equals(applicationType)) {
            return alternativeServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
        } else if (InterimApplicationType.DISPENSE_WITH_SERVICE.equals(applicationType)) {
            return dispenseWithServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
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
        } else if (AlternativeServiceType.BAILIFF.equals(serviceType)) {
            notificationDispatcher.send(bailiffApplicationSubmittedNotification, caseData, caseId);
            return;
        } else if (AlternativeServiceType.ALTERNATIVE_SERVICE.equals(serviceType)) {
            notificationDispatcher.send(alternativeServiceApplicationSubmittedNotification, caseData, caseId);
            return;
        } else if (AlternativeServiceType.DISPENSED.equals(serviceType)) {
            notificationDispatcher.send(dispenseServiceApplicationSubmittedNotification, caseData, caseId);
            return;
        }

        throw new UnsupportedOperationException();
    }
}
