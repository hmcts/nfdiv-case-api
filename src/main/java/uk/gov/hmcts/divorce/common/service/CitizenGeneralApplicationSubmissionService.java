package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.D11GeneralApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.SearchGovRecordsApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.D11GeneralApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.SearchGovRecordsApplicationGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;

@Service
@RequiredArgsConstructor
public class CitizenGeneralApplicationSubmissionService {
    private static final Set<GeneralApplicationType> AUTO_REFERRAL_TYPES = Set.of(
        GeneralApplicationType.DISCLOSURE_VIA_DWP
    );

    private final SearchGovRecordsApplicationGenerator searchGovRecordsApplicationGenerator;
    private final SearchGovRecordsApplicationSubmittedNotification searchGovApplicationSubmittedNotification;
    private final D11GeneralApplicationGenerator d11GeneralApplicationGenerator;
    private final D11GeneralApplicationSubmittedNotification d11GeneralApplicationSubmittedNotification;

    public boolean canBeAutoReferred(CaseData caseData, GeneralApplicationType generalApplicationType) {
        GeneralReferral generalReferral = caseData.getGeneralReferral();
        boolean caseAlreadyHasReferral = generalReferral != null && generalReferral.getGeneralReferralReason() != null;

        if (caseAlreadyHasReferral) {
            return false;
        }

        return AUTO_REFERRAL_TYPES.contains(generalApplicationType);
    }

    public void setEndState(CaseDetails<CaseData, State> details, GeneralApplication generalApplication) {
        final CaseData data = details.getData();
        final FeeDetails feeDetails = generalApplication.getGeneralApplicationFee();
        final ServicePaymentMethod paymentMethod = feeDetails.getPaymentMethod();


        final boolean isHwfApplication = ServicePaymentMethod.FEE_PAY_BY_HWF.equals(paymentMethod);
        final boolean isAwaitingDocuments = !YesOrNo.NO.equals(generalApplication.getGeneralApplicationDocsUploadedPreSubmission());

        if (data.isWelshApplication()) {
            data.getApplication().setWelshPreviousState(details.getState());
            details.setState(WelshTranslationReview);
        } else if (isAwaitingDocuments) {
            details.setState(AwaitingDocuments);
        } else if (isHwfApplication) {
            details.setState(AwaitingGeneralReferralPayment);
        } else {
            final boolean canBeAutoReferred = canBeAutoReferred(data, generalApplication.getGeneralApplicationType());

            details.setState(canBeAutoReferred ? AwaitingGeneralConsideration : GeneralApplicationReceived);
        }
    }

    public List<ListValue<DivorceDocument>> collectSupportingDocuments(InterimApplicationOptions userOptions) {
        boolean isD11GeneralApplication = DIGITISED_GENERAL_APPLICATION_D11.equals(userOptions.getInterimApplicationType());
        if (!isD11GeneralApplication) {
            return Collections.emptyList();
        }

        final GeneralApplicationD11JourneyOptions d11JourneyOptions = userOptions.getGeneralApplicationD11JourneyOptions();
        List<ListValue<DivorceDocument>> documents = new ArrayList<>();
        if (d11JourneyOptions.evidenceOfPartnerSupportRequired() && d11JourneyOptions.getPartnerAgreesDocs() != null) {
            documents.addAll(d11JourneyOptions.getPartnerAgreesDocs());
        }

        if (userOptions.hasUploadedSupportingDocuments()) {
            documents.addAll(d11JourneyOptions.getPartnerAgreesDocs());
        }

        return documents;
    }

    public DivorceDocument generateGeneralApplicationAnswerDocument(
        long caseId, Applicant applicant, CaseData caseData, GeneralApplication generalApplication
    ) {
        GeneralApplicationType generalApplicationType = generalApplication.getGeneralApplicationType();

        if (GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(generalApplicationType)) {
            return searchGovRecordsApplicationGenerator.generateDocument(caseId, applicant, caseData, generalApplication);
        } else if (generalApplicationType != null) {
            return DivorceDocument.builder().build();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void sendNotifications(long caseId, GeneralApplication generalApplication, CaseData caseData) {
        GeneralApplicationType generalApplicationType = generalApplication.getGeneralApplicationType();

        if (GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(generalApplication.getGeneralApplicationType())) {
            searchGovApplicationSubmittedNotification.sendToApplicant1(caseData, caseId, generalApplication);
        } else if (generalApplicationType != null) {
            return;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
