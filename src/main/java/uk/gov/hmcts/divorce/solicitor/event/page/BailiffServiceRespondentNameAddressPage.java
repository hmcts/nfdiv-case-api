package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class BailiffServiceRespondentNameAddressPage implements CcdPageConfiguration {

    private static final String RESPONDENTS_NAME_LABEL = "Enter the respondent's full name";

    private static final String RESPONDENT_IN_REFUGE_LABEL = "Is the respondent currently resident in a refuge?";

    private static final String RESPONDENT_ADDRESS_LABEL = """
            ## Which address (in England or Wales) should bailiff service be attempted at?

            Bailiff service can only be attempted at an address in England and Wales where postal delivery has already
            been tried. If the address below is not correct you should stop this application and update the respondent’s
            address so the papers can be resent to them. If the respondent still does not respond, you can then apply
            for bailiff service.
            """;

    private static final String HIDE_IF_CONFIDENTIAL = "applicant2ContactDetailsType = \"public\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceRespondentsNameAddressPage")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .mandatoryWithLabel(BailiffServiceJourneyOptions::getBailiffPartnersName, RESPONDENTS_NAME_LABEL)
                        .mandatoryWithLabel(BailiffServiceJourneyOptions::getBailiffPartnerInARefuge, RESPONDENT_IN_REFUGE_LABEL)
                    .done()
                .done()
            .done()
            .complex(CaseData::getApplicant2)
                .readonly(Applicant::getContactDetailsType, "applicant1BailiffPartnerInARefuge = \"NEVER_SHOW\"")
                .label("respondentAddressLabel", RESPONDENT_ADDRESS_LABEL, HIDE_IF_CONFIDENTIAL)
                .readonly(Applicant::getNonConfidentialAddress, HIDE_IF_CONFIDENTIAL)
            .done()
            .done();
    }
}
