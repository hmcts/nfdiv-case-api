package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.AlternativeServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.BailiffServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.DeemedServiceApplicationSubmittedNotification;
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
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;
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

    private final BailiffServiceApplicationSubmittedNotification bailiffApplicationSubmittedNotification;
    private final SearchGovRecordsApplicationSubmittedNotification searchGovRecordsApplicationNotifications;

    public DivorceDocument generateAnswerDocument(
        long caseId,
        Applicant applicant,
        CaseData caseData
    ) {
        InterimApplicationType applicationType = applicant.getInterimApplicationOptions().getInterimApplicationType();

        return switch (applicationType) {
            case DEEMED_SERVICE -> deemedServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
            case BAILIFF_SERVICE -> bailiffServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
            case ALTERNATIVE_SERVICE -> alternativeServiceApplicationGenerator.generateDocument(caseId, applicant, caseData);
            case SEARCH_GOV_RECORDS -> searchGovRecordsApplicationGenerator.generateDocument(caseId, applicant, caseData);
            case DISPENSE_WITH_SERVICE -> throw new UnsupportedOperationException("DISPENSE_WITH_SERVICE not yet implemented");
            case PROCESS_SERVER_SERVICE -> throw new UnsupportedOperationException("PROCESS_SERVER_SERVICE not yet implemented");
            default -> throw new UnsupportedOperationException();
        };
    }

    public void sendNotifications(
        long caseId,
        AlternativeServiceType serviceType,
        CaseData caseData
    ) {
        switch (serviceType) {
            case DEEMED -> notificationDispatcher.send(deemedApplicationSubmittedNotification, caseData, caseId);
            case BAILIFF -> notificationDispatcher.send(bailiffApplicationSubmittedNotification, caseData, caseId);
            case ALTERNATIVE_SERVICE -> notificationDispatcher
                .send(alternativeServiceApplicationSubmittedNotification, caseData, caseId);
            default -> throw new UnsupportedOperationException();
        }
    }

    public void sendGeneralApplicationNotifications(
        long caseId,
        GeneralApplication generalApplication,
        CaseData caseData
    ) {
        return switch (generalApplication.getGeneralApplicationType()) {
            case GeneralApplicationType.DISCLOSURE_VIA_DWP -> searchGovRecordsApplicationGenerator.generateDocument(caseId, applicant, caseData);
            default -> throw new UnsupportedOperationException();
        };
    }
}
