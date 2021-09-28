package uk.gov.hmcts.divorce.document.content.provider;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

@Component
public class ApplicantTemplateDataProvider {

    public String deriveFinancialOrder(final Applicant applicant) {

        final YesOrNo financialOrder = applicant.getFinancialOrder();

        if (null != financialOrder && financialOrder.toBoolean()) {

            final Set<FinancialOrderFor> financialOrderFor = applicant.getFinancialOrderFor();

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

    public String deriveApplicantPostalAddress(final Applicant applicant) {

        final AddressGlobalUK applicantHomeAddress = applicant.getHomeAddress();

        if (applicant.isRepresented()) {
            return applicant.getSolicitor().getAddress();
        } else if (null != applicantHomeAddress && !applicant.isConfidentialContactDetails()) {

            return Stream.of(
                    applicantHomeAddress.getAddressLine1(),
                    applicantHomeAddress.getAddressLine2(),
                    applicantHomeAddress.getAddressLine3(),
                    applicantHomeAddress.getPostTown(),
                    applicantHomeAddress.getCounty(),
                    applicantHomeAddress.getPostCode(),
                    applicantHomeAddress.getCountry())
                .filter(value -> null != value && !value.isEmpty())
                .collect(joining("\n"));
        }

        return null;
    }
}
