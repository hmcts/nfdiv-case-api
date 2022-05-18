package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.RejectionReason;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class ConditionalOrderRefusalContent {

    private static final String IS_SOLE = "isSole";
    private static final String IS_JOINT = "isJoint";
    private static final String LEGAL_ADVISOR_COMMENTS = "legalAdvisorComments";
    private static final String IS_CLARIFICATION = "isClarification";
    private static final String IS_AMENDED_APPLICATION = "isAmendedApplication";
    private static final String IS_OFFLINE = "isOffline";

    @Autowired
    private Clock clock;

    @Value("${court.locations.serviceCentre.serviceCentreName}")
    private String serviceCentre;

    @Value("${court.locations.serviceCentre.centreName}")
    private String centreName;

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.town}")
    private String town;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postcode;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        templateContent.put(CCD_CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
        }

        templateContent.put(LEGAL_ADVISOR_COMMENTS, generateLegalAdvisorComments(conditionalOrder));

        templateContent.put(IS_CLARIFICATION, MORE_INFO.equals(conditionalOrder.getRefusalDecision()));
        templateContent.put(IS_AMENDED_APPLICATION, REJECT.equals(conditionalOrder.getRefusalDecision()));
        templateContent.put(IS_OFFLINE, caseData.getApplicant1().isOffline());

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }

    private String generateLegalAdvisorComments(ConditionalOrder conditionalOrder) {

        if (MORE_INFO.equals(conditionalOrder.getRefusalDecision())) {

            Set<ClarificationReason> refusalClarificationReason = conditionalOrder.getRefusalClarificationReason();

            if (isEmpty(refusalClarificationReason)) {
                return "";
            }

            StringBuilder legalAdvisorComments = new StringBuilder();
            legalAdvisorComments.append(
                refusalClarificationReason.stream()
                    .map(ClarificationReason::getLabel)
                    .collect(Collectors.joining(". ", "", "."))
            );

            String refusalClarificationAdditionalInfo = conditionalOrder.getRefusalClarificationAdditionalInfo();
            if (isNotEmpty(refusalClarificationAdditionalInfo)) {
                legalAdvisorComments.append(String.format(" %s.", refusalClarificationAdditionalInfo));
            }

            return legalAdvisorComments.toString();

        } else {

            Set<RejectionReason> refusalRejectionReason = conditionalOrder.getRefusalRejectionReason();

            if (isEmpty(refusalRejectionReason)) {
                return "";
            }

            StringBuilder legalAdvisorComments = new StringBuilder();
            legalAdvisorComments.append(
                refusalRejectionReason.stream()
                    .map(RejectionReason::getLabel)
                    .collect(Collectors.joining(". ", "", "."))
            );

            String refusalRejectionAdditionalInfo = conditionalOrder.getRefusalRejectionAdditionalInfo();
            if (isNotEmpty(refusalRejectionAdditionalInfo)) {
                legalAdvisorComments.append(String.format(" %s.", refusalRejectionAdditionalInfo));
            }

            return legalAdvisorComments.toString();

        }
    }
}
