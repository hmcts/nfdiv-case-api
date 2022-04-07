package uk.gov.hmcts.divorce.document.content.provider;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;

import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP1_CONTACT_PRIVATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP1_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP2_CONTACT_PRIVATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_APP2_REPRESENTED;

@Component
public class ApplicantTemplateDataProvider {

    public String deriveJointFinancialOrder(final Applicant applicant) {

        final YesOrNo financialOrder = applicant.getFinancialOrder();

        if (null != financialOrder && financialOrder.toBoolean()) {

            final Set<FinancialOrderFor> financialOrderFor = applicant.getFinancialOrdersFor();

            if (financialOrderFor.contains(APPLICANT) && financialOrderFor.contains(CHILDREN)) {
                return "applicants, and for the children of both the applicants.";
            }

            if (financialOrderFor.contains(APPLICANT)) {
                return "applicants.";
            }

            if (financialOrderFor.contains(CHILDREN)) {
                return "children of both the applicants.";
            }
        }

        return null;
    }

    public String deriveJointFinancialOrder(Set<FinancialOrderFor> financialOrderFor) {

        if (!isEmpty(financialOrderFor)) {
            if (financialOrderFor.contains(APPLICANT) && financialOrderFor.contains(CHILDREN)) {
                return "applicants, and for the children of both the applicants.";
            }

            if (financialOrderFor.contains(APPLICANT)) {
                return "applicants.";
            }

            if (financialOrderFor.contains(CHILDREN)) {
                return "children of both the applicants.";
            }
        }

        return null;
    }

    public String deriveSoleFinancialOrder(final Applicant applicant) {

        final YesOrNo financialOrder = applicant.getFinancialOrder();

        if (null != financialOrder && financialOrder.toBoolean()) {

            final Set<FinancialOrderFor> financialOrderFor = applicant.getFinancialOrdersFor();

            if (financialOrderFor.contains(APPLICANT) && financialOrderFor.contains(CHILDREN)) {
                return "applicant, and for the children of the applicant and the respondent.";
            }

            if (financialOrderFor.contains(APPLICANT)) {
                return "applicant.";
            }

            if (financialOrderFor.contains(CHILDREN)) {
                return "children of the applicant and the respondent.";
            }
        }

        return null;
    }

    public void mapContactDetails(Applicant applicant1, Applicant applicant2, Map<String, Object> templateContent) {
        mapApplicantContactDetails(applicant1, APPLICANT_1_EMAIL, APPLICANT_1_POSTAL_ADDRESS, templateContent);

        mapApplicantContactDetails(applicant2, APPLICANT_2_EMAIL, APPLICANT_2_POSTAL_ADDRESS, templateContent);

        templateContent.put(IS_APP1_REPRESENTED, applicant1.isRepresented());
        templateContent.put(IS_APP1_CONTACT_PRIVATE, applicant1.isConfidentialContactDetails());
        templateContent.put(IS_APP2_REPRESENTED, applicant2.isRepresented());
        templateContent.put(IS_APP2_CONTACT_PRIVATE, applicant2.isConfidentialContactDetails());
    }

    private void mapApplicantContactDetails(Applicant applicant, String emailKey, String addressKey, Map<String, Object> templateContent) {
        templateContent.put(addressKey, applicant.getCorrespondenceAddress());
        if (applicant.isRepresented()) {
            templateContent.put(emailKey, applicant.getSolicitor().getEmail());
        } else if (!applicant.isConfidentialContactDetails()) {
            templateContent.put(emailKey, applicant.getEmail());
        } else {
            templateContent.put(emailKey, null);
        }
    }
}
