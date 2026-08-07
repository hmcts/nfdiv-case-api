package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceRespondentEmailAddress.LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE;

public class DispenseWithServiceRespondentTelephone implements CcdPageConfiguration {

    private static final String LABEL_RESPONDENTS_TELEPHONE_NUMBERS = """
        ### The respondent's telephone numbers ###

        If you have any telephone numbers for the respondent, provide them below. You will need to explain the attempts made by the
        applicant or someone else to contact them by telephone, and why it has not been successful. For example, this could be being told
        the number has not been recognised, or an unrecognised person answering the phone.

        The applicant should only attempt to contact the respondent if it is safe to do so.

        If you know the respondent partner has a telephone they actively use, you could consider applying for alternative service.
        """;

    private static final String HAVE_TELEPHONE_SHOW_CONDITION = "applicant1DispenseHavePartnerPhoneNumbers=\"Yes\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceRespondentPhone");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .label("labelRespondentTelephone", LABEL_RESPONDENTS_TELEPHONE_NUMBERS)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseHavePartnerPhoneNumbers)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerPhoneNumbers,
                            HAVE_TELEPHONE_SHOW_CONDITION)
                        .label("labelYouCanUploadEmailEvidence", LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE,
                            HAVE_TELEPHONE_SHOW_CONDITION)
                    .done()
                .done()
            .done();
    }
}
