package uk.gov.hmcts.divorce.document.content.provider;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
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

    public String deriveApplicantPostalAddress(final Applicant applicant) {

        final AddressGlobalUK applicantAddress = applicant.getAddress();

        // todo use new getCorrespondenceAddress
        if (applicant.isRepresented()) {
            return applicant.getSolicitor().getAddress();
        } else if (null != applicantAddress && !applicant.isConfidentialContactDetails()) {

            return Stream.of(
                    applicantAddress.getAddressLine1(),
                    applicantAddress.getAddressLine2(),
                    applicantAddress.getAddressLine3(),
                    applicantAddress.getPostTown(),
                    applicantAddress.getCounty(),
                    applicantAddress.getPostCode(),
                    applicantAddress.getCountry())
                .filter(value -> null != value && !value.isEmpty())
                .collect(joining("\n"));
        }

        return null;
    }

    public String deriveApplicant2PostalAddress(final Applicant applicant, final Application application) {

        // todo remove this if statement, see next comment
        if (applicant.isRepresented()) {
            return applicant.getSolicitor().getAddress();
        } else if (!applicant.isConfidentialContactDetails()) {
            // todo wrong, don't use isSolicitorApplication, just use new getCorrespondenceAddress method
            final AddressGlobalUK applicantAddress =
                application.isSolicitorApplication() ? applicant.getCorrespondenceAddress() : applicant.getAddress();

            // todo move to method to Address in ccd-config-lib
            if (null != applicantAddress) {
                return Stream.of(
                        applicantAddress.getAddressLine1(),
                        applicantAddress.getAddressLine2(),
                        applicantAddress.getAddressLine3(),
                        applicantAddress.getPostTown(),
                        applicantAddress.getCounty(),
                        applicantAddress.getPostCode(),
                        applicantAddress.getCountry()
                    )
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(joining("\n"));
            }
        }

        return null;
    }

    // todo kill this why is this different for sole applications???
    public String deriveSoleApplicationApplicant2PostalAddress(final Applicant applicant) {
        final AddressGlobalUK applicantAddress = applicant.getAddress();

        if (null != applicantAddress) {
            return Stream.of(
                applicantAddress.getAddressLine1(),
                applicantAddress.getAddressLine2(),
                applicantAddress.getAddressLine3(),
                applicantAddress.getPostTown(),
                applicantAddress.getCounty(),
                applicantAddress.getPostCode(),
                applicantAddress.getCountry()
            )
                .filter(value -> value != null && !value.isEmpty())
                .collect(joining("\n"));
        }
        return null;
    }
}
