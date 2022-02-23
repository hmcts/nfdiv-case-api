package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_SIX_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_TWELVE_MONTHS;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION;

@Component
public class ApplicationTransformer implements Function<TransformationDetails, TransformationDetails> {
    private static final String APPLICANT_2 = "applicant2";
    private static final String RESPONDENT = "respondent";
    private static final String APPLICANT_APPLICANT_1 = "applicant,applicant1";
    private static final int HWF_NO_VALID_LENGTH = 6;

    @Autowired
    private Clock clock;

    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();
        caseData.getApplication().getJurisdiction().setConnections(
            deriveJurisdictionConnections(ocrDataFields, transformationDetails.getTransformationWarnings())
        );

        caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));
        setMarriageBrokenDetails(transformationDetails);
        setCourtFee(transformationDetails);
        return transformationDetails;
    }

    private void setMarriageBrokenDetails(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        if (SOLE_APPLICATION.equals(caseData.getApplicationType())
            && (!toBoolean(ocrDataFields.getSoleOrApplicant1ConfirmationOfBreakdown())
            || toBoolean(ocrDataFields.getApplicant2ConfirmationOfBreakdown()))) {
            transformationDetails.getTransformationWarnings().add(
                "Please review confirmation of breakdown for sole application in the scanned form"
            );
        }
        if (JOINT_APPLICATION.equals(caseData.getApplicationType())
            && (!toBoolean(ocrDataFields.getSoleOrApplicant1ConfirmationOfBreakdown())
            || !toBoolean(ocrDataFields.getApplicant2ConfirmationOfBreakdown()))) {
            transformationDetails.getTransformationWarnings().add(
                "Please review confirmation of breakdown for joint application in the scanned form"
            );
        }
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(
            from(toBoolean(ocrDataFields.getSoleOrApplicant1ConfirmationOfBreakdown()))
        );
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(
            from(toBoolean(ocrDataFields.getApplicant2ConfirmationOfBreakdown()))
        );
    }

    private Set<JurisdictionConnections> deriveJurisdictionConnections(OcrDataFields ocrDataFields, List<String> warnings) {
        Set<JurisdictionConnections> connections = new HashSet<>();
        if (toBoolean(ocrDataFields.getJurisdictionReasonsBothPartiesHabitual())) {
            connections.add(APP_1_APP_2_RESIDENT);
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasonsBothPartiesLastHabitual())) {
            connections.add(APP_1_APP_2_LAST_RESIDENT);
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasonsRespHabitual())) {
            connections.add(APP_2_RESIDENT);
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasonsJointHabitual())) {
            connections.add(APP_1_RESIDENT_JOINT);
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasons1YrHabitual())) {
            connections.add(APP_1_RESIDENT_TWELVE_MONTHS);
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasons6MonthsHabitual())) {
            connections.add(APP_1_RESIDENT_SIX_MONTHS);
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasonsBothPartiesDomiciled())) {
            connections.add(APP_1_APP_2_DOMICILED);
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasonsOnePartyDomiciled())) {
            if (APPLICANT_APPLICANT_1.equalsIgnoreCase(ocrDataFields.getJurisdictionReasonsOnePartyDomiciledWho())) {
                connections.add(APP_1_DOMICILED);
            } else if (APPLICANT_2.equalsIgnoreCase(ocrDataFields.getJurisdictionReasonsOnePartyDomiciledWho())
                || RESPONDENT.equalsIgnoreCase(ocrDataFields.getJurisdictionReasonsOnePartyDomiciledWho())) {
                connections.add(APP_2_DOMICILED);
            } else {
                warnings.add("Please verify jurisdiction connections(missing/invalid domiciled who) in scanned form");
            }
        }
        if (toBoolean(ocrDataFields.getJurisdictionReasonsSameSex())) {
            // only for civil partnership/same-sex
            connections.add(RESIDUAL_JURISDICTION);
        }

        if (isEmpty(connections)) {
            warnings.add("Please verify jurisdiction connections(no options selected) in scanned form");
        }
        return connections;
    }

    private void setCourtFee(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        if (isNotEmpty(ocrDataFields.getSoleOrApplicant1HWFNo())
            && ocrDataFields.getSoleOrApplicant1HWFNo().length() != HWF_NO_VALID_LENGTH) {
            transformationDetails.getTransformationWarnings().add("Please review HWF number for applicant1 in scanned form");
        }

        if (isNotEmpty(ocrDataFields.getApplicant2HWFNo())
            && ocrDataFields.getApplicant2HWFNo().length() != HWF_NO_VALID_LENGTH) {
            transformationDetails.getTransformationWarnings().add("Please review HWF number for applicant2 in scanned form");
        }

        caseData.getApplication().setApplicant1HelpWithFees(
            HelpWithFees
                .builder()
                .appliedForFees(from(toBoolean(ocrDataFields.getSoleOrApplicant1HWFConfirmation())))
                .referenceNumber(ocrDataFields.getSoleOrApplicant1HWFNo())
                .needHelp(from(toBoolean(ocrDataFields.getSoleOrApplicant1HWFApp())))
                .build()
        );
        caseData.getApplication().setApplicant2HelpWithFees(
            HelpWithFees
                .builder()
                .appliedForFees(from(toBoolean(ocrDataFields.getApplicant2HWFConfirmation())))
                .referenceNumber(ocrDataFields.getApplicant2HWFNo())
                .needHelp(from(toBoolean(ocrDataFields.getApplicant2HWFApp())))
                .build()
        );
    }
}
