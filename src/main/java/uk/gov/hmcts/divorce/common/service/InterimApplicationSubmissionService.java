package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.AlternativeServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.BailiffServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.D11GeneralApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.DeemedServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.DispenseServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.SearchGovRecordsApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.AlternativeServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.BailiffServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.D11GeneralApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.DispenseWithServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.SearchGovRecordsApplicationGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

@Service
@RequiredArgsConstructor
public class InterimApplicationSubmissionService {
    private final NotificationDispatcher notificationDispatcher;

    private final BailiffServiceApplicationGenerator bailiffServiceApplicationGenerator;
    private final DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;
    private final DeemedServiceApplicationSubmittedNotification deemedApplicationSubmittedNotification;
    private final AlternativeServiceApplicationSubmittedNotification alternativeServiceApplicationSubmittedNotification;
    private final AlternativeServiceApplicationGenerator alternativeServiceApplicationGenerator;
    private final SearchGovRecordsApplicationGenerator searchGovRecordsApplicationGenerator;
    private final SearchGovRecordsApplicationSubmittedNotification searchGovApplicationSubmittedNotification;
    private final BailiffServiceApplicationSubmittedNotification bailiffApplicationSubmittedNotification;
    private final DispenseServiceApplicationSubmittedNotification dispenseServiceApplicationSubmittedNotification;
    private final DispenseWithServiceApplicationGenerator dispenseWithServiceApplicationGenerator;
    private final D11GeneralApplicationGenerator d11GeneralApplicationGenerator;
    private final D11GeneralApplicationSubmittedNotification d11GeneralApplicationSubmittedNotification;

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

    public void sendServiceApplicationNotifications(long caseId, AlternativeServiceType serviceType, CaseData caseData) {
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

    public DivorceDocument generateGeneralApplicationAnswerDocument(
        long caseId, Applicant applicant, CaseData caseData, GeneralApplication generalApplication
    ) {
        GeneralApplicationType generalApplicationType = generalApplication.getGeneralApplicationType();

        if (GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(generalApplicationType)) {
            return searchGovRecordsApplicationGenerator.generateDocument(caseId, applicant, caseData, generalApplication);
        } else if (generalApplicationType != null) {
            return d11GeneralApplicationGenerator.generateDocument(caseId, applicant, caseData, generalApplication);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void sendGeneralApplicationNotifications(long caseId, GeneralApplication generalApplication, CaseData caseData) {
        GeneralApplicationType generalApplicationType = generalApplication.getGeneralApplicationType();

        if (GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(generalApplication.getGeneralApplicationType())) {
            searchGovApplicationSubmittedNotification.sendToApplicant1(caseData, caseId, generalApplication);
        } else if (generalApplicationType != null) {
            d11GeneralApplicationSubmittedNotification.sendToApplicant1(caseData, caseId, generalApplication);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
