package uk.gov.hmcts.divorce.solicitor.event.page;

import org.apache.commons.validator.routines.EmailValidator;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;

public class Applicant2ServiceDetails implements CcdPageConfiguration {

    private static final String INVALID_APPLICANT_EMAIL_ERROR = "You have entered an invalid applicant email address. "
        + "Please check the email and enter it again, before submitting the application.";

    private static final String INVALID_SOLICITOR_EMAIL_ERROR = "You have entered an invalid solicitor email address. "
        + "Please check the email and enter it again, before submitting the application.";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("Applicant2ServiceDetails", this::midEvent)
            .pageLabel("Service details")
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getSolicitorRepresented, "Is ${labelContentTheApplicant2} represented by a solicitor?")
                .complex(Applicant::getSolicitor)

                    .mandatory(Solicitor::getName,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor's full name",
                        "Enter the full name of the individual solicitor who will be dealing with the case")

                    .optional(Solicitor::getReference,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor's reference",
                        "The internal reference that the solicitor’s firm uses to identify the case")

                    .optional(Solicitor::getPhone,
                "applicant2SolicitorRepresented=\"Yes\"",
                null,
                "${labelContentApplicant2UC} solicitor's phone number",
                "The solicitor’s direct phone number")

                    .mandatory(Solicitor::getEmail,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor’s email address",
                        "The solicitor’s direct email address")

                    .mandatory(Solicitor::getAddress,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor's postal address",
                        "solicitor’s postal address")

                    .mandatory(Solicitor::getAddressOverseas,
                            "applicant2SolicitorRepresented=\"Yes\"",
                            null,
                            "Is ${labelContentApplicant2UC} solicitor's postal address international?",
                            "Is solicitor's postal address international?")

                    .complex(Solicitor::getOrganisationPolicy, "applicant2SolicitorRepresented=\"Yes\"")
                        .complex(OrganisationPolicy::getOrganisation)
                            .optional(Organisation::getOrganisationId)
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_2_SOLICITOR)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                        .done()
                    .done()

                .label("respondents-service-details-heading",
                    "# ${labelContentApplicant2UC} service details",
                    "applicant2SolicitorRepresented=\"No\"")

                .label("respondents-service-details-text1",
                    "It’s important you provide the respondent’s email address so the court can serve documents to them online. "
                        + "Otherwise the papers will be served by post, which will take longer",
                    "applicant2SolicitorRepresented=\"No\" AND applicationType=\"soleApplication\"")

                .label("respondents-service-details-text2",
                    "You should also provide a postal address so that they can be sent a paper copy of the Notice Of Proceedings. "
                        + "If you only provide the email address, you will need to apply to serve by email only",
                    "applicant2SolicitorRepresented=\"No\" AND applicationType=\"soleApplication\"")

                .label("respondents-service-details-text3",
                    "If you need to make any separate applications relating to service then you can do this after you have submitted"
                        + " this application, using the ‘general application’ event.",
                    "applicant2SolicitorRepresented=\"No\" AND applicationType=\"soleApplication\"")

                .optional(Applicant::getEmail,
                    "applicant2SolicitorRepresented=\"Yes\" OR applicant2SolicitorRepresented=\"No\"",
                        null,
                        "${labelContentApplicant2UC} email address",
                        "Enter the email address which they actively use for personal emails. "
                            + "You should avoid using their work email address because it may not be private.")

                .mandatory(Applicant::getAddress,
                    "applicant2SolicitorRepresented=\"Yes\" OR applicant2SolicitorRepresented=\"No\"",
                    null,
                    "${labelContentApplicant2UC} postal address",
                    "This address will be used to notify them about the application")
                .mandatory(Applicant::getAddressOverseas,
                    "applicant2SolicitorRepresented=\"Yes\" OR applicant2SolicitorRepresented=\"No\"",
                    null,
                    "Is ${labelContentApplicant2UC} postal address international?")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        Applicant applicant2 = caseData.getApplicant2();
        List<String> validationErrors = new ArrayList<>();

        boolean validApplicantEmail;
        if (isNotEmpty(applicant2.getEmail())) {
            validApplicantEmail = EmailValidator.getInstance().isValid(applicant2.getEmail());
            if (!validApplicantEmail) {
                validationErrors.add(INVALID_APPLICANT_EMAIL_ERROR);
            }
        }

        if (applicant2.isRepresented()) {
            boolean validSolicitorEmail = EmailValidator.getInstance().isValid(applicant2.getSolicitor().getEmail());
            if (!validSolicitorEmail) {
                validationErrors.add(INVALID_SOLICITOR_EMAIL_ERROR);
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(validationErrors)
            .build();
    }
}
