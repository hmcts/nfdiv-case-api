package uk.gov.hmcts.divorce.solicitor.event.page.serviceapplicationpages;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AlternativeServiceDetailsAndUploadPage implements CcdPageConfiguration {

    private static final String DOC_UPLOAD_PAGE_SHOW_CONDITION = "applicant1InterimAppsCanUploadEvidence=\"Yes\"";
    private static final String RESPONDENT_NAME_TEXT = "- the respondent's name";
    private static final String MESSAGE_SENT_TEXT = "- the date the messages were sent";
    private static final String RESPONDENT_EMAIL = "- the respondent's email address";
    private static final String RESPONDENT_EMAIL_SHOW_CONDITION = "applicant1AltServiceMethod != \"inADifferentWay\"";
    private static final String RESPONDENT_PHONE = "- the respondent's phone number";
    private static final String RESPONDENT_PHONE_SHOW_CONDITION =
        "applicant1AltServiceDifferentWaysCONTAINS\"whatsapp\" OR applicant1AltServiceDifferentWaysCONTAINS\"textMessage\"";
    private static final String RESPONDENT_SOCIAL_MEDIA = "- the respondent's social media username";
    private static final String RESPONDENT_SOCIAL_MEDIA_SHOW_CONDITION = "applicant1AltServiceDifferentWaysCONTAINS\"socialMedia\"";
    private static final String EVIDENCE_LABEL = """
        If your evidence is a conversation in a language other than English, you'll need to provide a
        <a href="https://www.gov.uk/certifying-a-document#certifying-a-translation" target="_blank">certified translation</a>


         ## Applicant 1 uploaded documents
        """;
    private static final String ALTERNATIVE_EVIDENCE_DOCS_LABEL = """
        ## Upload your documents

        Upload your evidence to support your application for alternative service.

        Your evidence should show that the respondent actively uses the email address, phone number or social media platform you want
         to use to send the papers.

        It may be helpful if your images show:


        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("alternativeServiceDetailsAndUpload");

        page.label("alternativeEvidenceDocsLabel", ALTERNATIVE_EVIDENCE_DOCS_LABEL)
            .showCondition(isNotBlank(pageShowCondition)
                ? PageBuilder.andShowCondition(DOC_UPLOAD_PAGE_SHOW_CONDITION, pageShowCondition)
                : DOC_UPLOAD_PAGE_SHOW_CONDITION)
            .label("respondentNameText", RESPONDENT_NAME_TEXT)
            .label("messageSentText", MESSAGE_SENT_TEXT)
            .label("respondentEmail", RESPONDENT_EMAIL, RESPONDENT_EMAIL_SHOW_CONDITION)
            .label("respondentPhone", RESPONDENT_PHONE, RESPONDENT_PHONE_SHOW_CONDITION)
            .label("respondentSocialMedia", RESPONDENT_SOCIAL_MEDIA, RESPONDENT_SOCIAL_MEDIA_SHOW_CONDITION)
            .label("alternativeEvidenceLabel", EVIDENCE_LABEL)
            .complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .optionalWithLabel(InterimApplicationOptions::getInterimAppsEvidenceDocs, "Applicant 1 uploaded documents")
            .done();
    }
}
