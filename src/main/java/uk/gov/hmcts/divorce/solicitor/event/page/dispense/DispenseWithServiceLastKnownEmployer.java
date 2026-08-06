package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceRespondentEmailAddress.LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE;

public class DispenseWithServiceLastKnownEmployer implements CcdPageConfiguration {

    private static final String LABEL_LAST_KNOWN_EMPLOYER = """
        ### Contacting the respondent's last known employer ###

        If you know where the respondent last worked, you should try contacting the employer. The may be able to confirm whether the
        respondent still works there, or help you trace the respondent.

        If the employer can confirm that the respondent still works there, you should not proceed with this application to dispense
        with service. You could instead consider hiring a process server to deliver the papers to their work address.

        You could also ask the employer to send a stamped envelope containing the papers to the respondent on behalf of the applicant.
        We will send you the papers on request. You do not need to tell the employer what the envelope contains.

        Once the employer confirms that they have sent the envelope, if the respondent does not respond within 14days you can continue this
        application.
        """;

    private static final String TRIED_CONTACT_WITH_EMPLOYER_YES = "applicant1DispenseTriedContactingEmployer=\"Yes\"";
    private static final String TRIED_CONTACT_WITH_EMPLOYER_NO = "applicant1DispenseTriedContactingEmployer=\"No\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceTriedEmployer")
                    .pageLabel("Dispense with service app");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getLabelContent)
            .readonlyNoSummary(LabelContent::getUnionType, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .label("labelRespondentLastKnownEmployer", LABEL_LAST_KNOWN_EMPLOYER)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseTriedContactingEmployer)
                        .label("labelContactEmployerResults", "### Enter the employer's details ###",
                            TRIED_CONTACT_WITH_EMPLOYER_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseEmployerName,
                            TRIED_CONTACT_WITH_EMPLOYER_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseEmployerAddress,
                            TRIED_CONTACT_WITH_EMPLOYER_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerOccupation,
                            TRIED_CONTACT_WITH_EMPLOYER_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseContactingEmployerResults,
                            TRIED_CONTACT_WITH_EMPLOYER_YES)
                        .label("labelYouCanUploadEmployerEvidence", LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE,
                            TRIED_CONTACT_WITH_EMPLOYER_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseWhyNoContactingEmployer,
                            TRIED_CONTACT_WITH_EMPLOYER_NO)
                    .done()
                .done()
            .done();
    }
}
