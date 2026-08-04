package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DispenseWithServiceRespondentEmailAddress implements CcdPageConfiguration {

    private static final String LABEL_ABLE_TO_UPLOAD_EMAIL_EVIDENCE =
        "You will be able to upload any evidence you have at the end of this application";
    private static final String LABEL_RESPONDENTS_EMAIL_ADDRESS = """
        ### The respondent's email address ###

        If you have any email addresses for the respondent, you will need to provide them to the court. You will need to show why the
        respondent can no longer be contacted on this email address. For example, this could be receiving a 'delivery failed' email when
        trying to contact that email address.

        If you know the respondent has an email address they actively use, you could consider applying for alternative service.
        """;

    private static final String HAVE_EMAIL_SHOW_CONDITION = "applicant1DispenseHavePartnerEmailAddresses=\"Yes\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceRespondentEmail")
                    .pageLabel("Dispense with service app");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .label("labelRespondentEmailAddress", LABEL_RESPONDENTS_EMAIL_ADDRESS)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseHavePartnerEmailAddresses)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerEmailAddresses,
                            HAVE_EMAIL_SHOW_CONDITION)
                        .label("labelYouCanUploadEmailEvidence", LABEL_ABLE_TO_UPLOAD_EMAIL_EVIDENCE,
                            HAVE_EMAIL_SHOW_CONDITION)
                    .done()
                .done()
            .done();
    }
}
