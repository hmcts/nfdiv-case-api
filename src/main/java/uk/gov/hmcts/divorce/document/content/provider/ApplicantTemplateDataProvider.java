package uk.gov.hmcts.divorce.document.content.provider;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;

import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

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
}
