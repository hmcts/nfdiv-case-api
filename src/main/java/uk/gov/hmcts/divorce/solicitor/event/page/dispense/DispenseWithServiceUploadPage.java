package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DispenseWithServiceUploadPage implements CcdPageConfiguration {

    private static final String DISPENSE_UPLOAD_EVIDENCE_LABEL1 = """
            ## Upload your documents

            If you have any evidence to support your application, you can upload it now. This may include:
            """;

    private static final String DISPENSE_UPLOAD_LIST_LABEL1 = "- your no trace certificate. (Mandatory)";
    private static final String DISPENSE_UPLOAD_LIST_LABEL2 = "- proof of any attempts you have made to contact "
        + "the respondent on their email addresses. (Optional)";
    private static final String DISPENSE_UPLOAD_LIST_LABEL3 = "- proof of any attempts to contact "
        + "the respondent on their phone numbers. (Optional)";
    private static final String DISPENSE_UPLOAD_LIST_LABEL4 = "- results of the search by a tracing agent. (Optional)";
    private static final String DISPENSE_UPLOAD_LIST_LABEL5 = "- results of the search online by people finding services. (Optional)";
    private static final String DISPENSE_UPLOAD_LIST_LABEL6 = "- results of any any online searches you've carried out using "
        + "search engines or social media platforms. (Optional)";
    private static final String DISPENSE_UPLOAD_LIST_LABEL7 = "- proof of enquiries with the respondent's last know employer. (Optional)";
    private static final String DISPENSE_UPLOAD_LIST_LABEL8 = "- proof of any other enquiries made. (Optional)";

    private static final String SHOW_CONDITION_YES_SEARCHED_FINAL_ORDER = "applicant1DispenseHaveSearchedFinalOrder=\"Yes\"";
    private static final String SHOW_CONDITION_YES_TRACING_AGENT = "applicant1DispenseTriedTracingAgent=\"Yes\"";
    private static final String SHOW_CONDITION_YES_SEARCH_ONLINE = "applicant1DispenseTriedSearchingOnline=\"Yes\"";
    private static final String SHOW_CONDITION_YES_TRACING_ONLINE = "applicant1DispenseTriedTracingOnline=\"Yes\"";
    private static final String SHOW_CONDITION_YES_CONTACT_EMPLOYER = "applicant1DispenseTriedContactingEmployer=\"Yes\"";
    private static final String SHOW_CONDITION_YES_CONTACT_EMAIL = "applicant1DispenseHavePartnerEmailAddresses=\"Yes\"";
    private static final String SHOW_CONDITION_YES_CONTACT_PHONE = "applicant1DispenseHavePartnerPhoneNumbers=\"Yes\"";



    private static final String DISPENSE_UPLOAD_EVIDENCE_END_LABEL = """
            If you're uploading images or screenshots of a recent conversation by text, email or
            social media, make sure they include:

            - the respondent's name
            - the date the messages were sent
            - the respondent's email address, phone number or social media username as appropriate

            If your evidence is a conversation in a language other than English, you'll need to provide a
            <a href="https://www.gov.uk/certifying-a-document#certifying-a-translation" target="_blank">certified translation</a>

            You may need to upload multiple documents.

            The court cannot accept video or audio recordings as evidence.

            The file must be in jpeg, tiff, png or pdf format.
            """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceUploadDocs");
        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .label("dispenseEvidenceLabel1", DISPENSE_UPLOAD_EVIDENCE_LABEL1)
                        .label("dispenseEvidenceListLabel1", DISPENSE_UPLOAD_LIST_LABEL1, SHOW_CONDITION_YES_SEARCHED_FINAL_ORDER)
                        .label("dispenseEvidenceListLabel2", DISPENSE_UPLOAD_LIST_LABEL2, SHOW_CONDITION_YES_CONTACT_EMAIL)
                        .label("dispenseEvidenceListLabel3", DISPENSE_UPLOAD_LIST_LABEL3, SHOW_CONDITION_YES_CONTACT_PHONE)
                        .label("dispenseEvidenceListLabel4", DISPENSE_UPLOAD_LIST_LABEL4, SHOW_CONDITION_YES_TRACING_AGENT)
                        .label("dispenseEvidenceListLabel5", DISPENSE_UPLOAD_LIST_LABEL5, SHOW_CONDITION_YES_TRACING_ONLINE)
                        .label("dispenseEvidenceListLabel6", DISPENSE_UPLOAD_LIST_LABEL6, SHOW_CONDITION_YES_SEARCH_ONLINE)
                        .label("dispenseEvidenceListLabel7", DISPENSE_UPLOAD_LIST_LABEL7, SHOW_CONDITION_YES_CONTACT_EMPLOYER)
                        .label("dispenseEvidenceListLabel8", DISPENSE_UPLOAD_LIST_LABEL8)
                        .label("dispenseEvidenceEndLabel", DISPENSE_UPLOAD_EVIDENCE_END_LABEL)
                    .done()
                    .optionalWithLabel(InterimApplicationOptions::getInterimAppsEvidenceDocs, "Uploaded documents")
                .done()
            .done();
    }
}
