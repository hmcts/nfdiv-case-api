package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class BailiffServiceRespondentServiceTimeVehiclePage implements CcdPageConfiguration {

    private static final String BEST_TIME_TO_SERVE_LABEL = "When is best for the bailiff to serve the papers on the respondent?";
    private static final String BEST_TIME_TO_SERVE_HINT = """
        For example, enter “Tuesday, between 8am and 2pm” if you believe the bailiff should be able to
        serve the papers within that time or enter “Tuesday at 12pm” if you believe the bailiff should be
        able to serve the papers at midday
        """;

    private static final String RESPONDENT_ACCESS_TO_VEHICLE_LABEL = "Does the respondent have access to a vehicle?";

    private static final String RESPONDENT_VEHICLE_KNOWN = "applicant1BailiffDoesPartnerHaveVehicle = \"Yes\"";

    private static final String RESPONDENT_VEHICLE_DETAILS_LABEL = """
        ### Details of the respondent's vehicle
        If you're not sure, leave the field blank.
        """;

    private static final String RESPONDENT_VEHICLE_MODEL_LABEL = "Manufacturer and model";
    private static final String RESPONDENT_VEHICLE_MODEL_HINT = "For example, Ford Fiesta";

    private static final String RESPONDENT_VEHICLE_COLOUR_LABEL = "Colour";
    private static final String RESPONDENT_VEHICLE_COLOUR_HINT = "For example, red";

    private static final String RESPONDENT_VEHICLE_REGISTRATION_LABEL = "Registration number";
    private static final String RESPONDENT_VEHICLE_REGISTRATION_HINT = "For example, GF08 RGH";

    private static final String RESPONDENT_OTHER_VEHICLES_LABEL = "Details of the respondent's other vehicles";
    private static final String RESPONDENT_OTHER_VEHICLES_HINT = "Provide the Manufacturer and model, Colour, Registration number";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceRespondentsServiceTimeVehiclePage")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .mandatory(
                            BailiffServiceJourneyOptions::getBailiffBestTimeToServe,
                            ALWAYS_SHOW,
                            NO_DEFAULT_VALUE,
                            BEST_TIME_TO_SERVE_LABEL,
                            BEST_TIME_TO_SERVE_HINT
                        )
                        .mandatoryWithLabel(
                            BailiffServiceJourneyOptions::getBailiffDoesPartnerHaveVehicle,
                            RESPONDENT_ACCESS_TO_VEHICLE_LABEL
                        )
                        .label("bailiffPartnerVehicleDetailsLabel", RESPONDENT_VEHICLE_DETAILS_LABEL, RESPONDENT_VEHICLE_KNOWN)
                        .optional(
                            BailiffServiceJourneyOptions::getBailiffPartnerVehicleModel,
                            RESPONDENT_VEHICLE_KNOWN,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_VEHICLE_MODEL_LABEL,
                            RESPONDENT_VEHICLE_MODEL_HINT
                        )
                        .optional(
                            BailiffServiceJourneyOptions::getBailiffPartnerVehicleColour,
                            RESPONDENT_VEHICLE_KNOWN,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_VEHICLE_COLOUR_LABEL,
                            RESPONDENT_VEHICLE_COLOUR_HINT
                        )
                        .optional(
                            BailiffServiceJourneyOptions::getBailiffPartnerVehicleRegistration,
                            RESPONDENT_VEHICLE_KNOWN,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_VEHICLE_REGISTRATION_LABEL,
                            RESPONDENT_VEHICLE_REGISTRATION_HINT
                        )
                        .optional(
                            BailiffServiceJourneyOptions::getBailiffPartnerVehicleOtherDetails,
                            RESPONDENT_VEHICLE_KNOWN,
                            NO_DEFAULT_VALUE,
                            RESPONDENT_OTHER_VEHICLES_LABEL,
                            RESPONDENT_OTHER_VEHICLES_HINT
                        )
                    .done()
                .done()
            .done()
            .done();
    }
}
