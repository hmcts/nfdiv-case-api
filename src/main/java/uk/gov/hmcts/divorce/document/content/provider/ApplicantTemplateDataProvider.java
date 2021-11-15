package uk.gov.hmcts.divorce.document.content.provider;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@Component
public class ApplicantTemplateDataProvider {

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

    public String deriveApplicant2PostalAddress(final Applicant applicant, final Application application) {

        if (applicant.isRepresented()) {
            return applicant.getSolicitor().getAddress();
        } else if (!applicant.isConfidentialContactDetails()) {

            final AddressGlobalUK applicantHomeAddress =
                application.isSolicitorApplication() ? applicant.getCorrespondenceAddress() : applicant.getHomeAddress();

            if (null != applicantHomeAddress) {
                return Stream.of(
                        applicantHomeAddress.getAddressLine1(),
                        applicantHomeAddress.getAddressLine2(),
                        applicantHomeAddress.getAddressLine3(),
                        applicantHomeAddress.getPostTown(),
                        applicantHomeAddress.getCounty(),
                        applicantHomeAddress.getPostCode(),
                        applicantHomeAddress.getCountry()
                    )
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(joining("\n"));
            }
        }

        return null;
    }
}
