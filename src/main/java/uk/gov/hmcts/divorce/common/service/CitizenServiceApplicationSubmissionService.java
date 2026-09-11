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
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.AlternativeServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.BailiffServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.DispenseWithServiceApplicationGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Service
@RequiredArgsConstructor
public class CitizenServiceApplicationSubmissionService {
    private final NotificationDispatcher notificationDispatcher;

    private final BailiffServiceApplicationGenerator bailiffServiceApplicationGenerator;
    private final DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;
    private final DeemedServiceApplicationSubmittedNotification deemedApplicationSubmittedNotification;
    private final AlternativeServiceApplicationSubmittedNotification alternativeServiceApplicationSubmittedNotification;
    private final AlternativeServiceApplicationGenerator alternativeServiceApplicationGenerator;
    private final BailiffServiceApplicationSubmittedNotification bailiffApplicationSubmittedNotification;
    private final DispenseServiceApplicationSubmittedNotification dispenseServiceApplicationSubmittedNotification;
    private final DispenseWithServiceApplicationGenerator dispenseWithServiceApplicationGenerator;

    public DivorceDocument generateServiceApplicationAnswerDocument(long caseId, Applicant applicant, CaseData caseData) {
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

    public void sendNotifications(long caseId, AlternativeServiceType serviceType, CaseData caseData) {
        switch (serviceType) {
            case DEEMED -> notificationDispatcher.send(deemedApplicationSubmittedNotification, caseData, caseId);
            case BAILIFF -> notificationDispatcher.send(bailiffApplicationSubmittedNotification, caseData, caseId);
            case ALTERNATIVE_SERVICE -> notificationDispatcher
                .send(alternativeServiceApplicationSubmittedNotification, caseData, caseId);
            case DISPENSED -> notificationDispatcher
                .send(dispenseServiceApplicationSubmittedNotification, caseData, caseId);
            default -> throw new UnsupportedOperationException();
        }
    }
}
