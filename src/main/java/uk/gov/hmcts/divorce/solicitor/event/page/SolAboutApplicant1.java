package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing;

import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;

@Slf4j
public class SolAboutApplicant1 implements CcdPageConfiguration {

    private static final String DARK_HORIZONTAL_RULE =
        "![Dark Rule](https://raw.githubusercontent.com/hmcts/nfdiv-case-api/master/resources/image/LabelDarkHorizontalRule.png)";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant1", this::midEvent)
            .pageLabel("About the applicant")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getFirstName,
                    "${labelContentApplicantsOrApplicant1s} first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentApplicantsOrApplicant1s} middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentApplicantsOrApplicant1s} last name")
                .mandatoryWithLabel(Applicant::getNameDifferentToMarriageCertificate,
                    "Is ${labelContentTheApplicantOrApplicant1} name different to that on the"
                        + " ${labelContentMarriageOrCivilPartnership} certificate?")
                .mandatoryWithoutDefaultValue(Applicant::getNameChangedHow,
                "applicant1NameDifferentToMarriageCertificate=\"Yes\"",
                "What evidence will be provided for the name change? ")
                .mandatoryWithoutDefaultValue(Applicant::getNameChangedHowOtherDetails,
                "applicant1NameChangedHow=\"other\"",
                "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .mandatoryWithoutDefaultValue(Applicant::getGender, "divorceOrDissolution=\"dissolution\"",
                "Is ${labelContentTheApplicantOrApplicant1} male or female?")
                .done()
            .complex(CaseData::getApplication)
                .mandatory(Application::getDivorceWho, "divorceOrDissolution=\"divorce\"", null,
                "Who is ${labelContentTheApplicantOrApplicant1} divorcing?")
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getFormationType)
                    .done()
                .done()
            .label("contactDetails", "# ${labelContentApplicantsOrApplicant1s} contact details")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getEmail,
                    "${labelContentApplicantsOrApplicant1s} email address")
                .optionalWithLabel(Applicant::getPhoneNumber,
                    "${labelContentApplicantsOrApplicant1s} phone number")
                .mandatoryWithLabel(Applicant::getHomeAddress,
                    "${labelContentApplicantsOrApplicant1s} home address")
                .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
                .mandatory(Applicant::getContactDetailsType)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for SolAboutTheSolicitor");

        var data = details.getData();
        Gender app1Gender;
        Gender app2Gender;
        WhoDivorcing whoDivorcing;

        if (data.getDivorceOrDissolution().isDivorce()) {
            // for a divorce we ask who is applicant1 divorcing to infer applicant2's gender, then use the marriage
            // formation to infer applicant 1's gender
            whoDivorcing = data.getApplication().getDivorceWho();
            app2Gender = whoDivorcing == HUSBAND ? MALE : FEMALE;
            app1Gender = data.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app2Gender);
        } else {
            // for a dissolution we ask for applicant1's gender and use the marriage formation to infer applicant 2's
            // gender and who they are divorcing
            app1Gender = data.getApplicant1().getGender();
            app2Gender = data.getApplication().getMarriageDetails().getFormationType().getPartnerGender(app1Gender);
            whoDivorcing = app2Gender == MALE ? HUSBAND : WIFE;
        }

        data.getApplicant1().setGender(app1Gender);
        data.getApplicant2().setGender(app2Gender);
        data.getApplication().setDivorceWho(whoDivorcing);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

}
