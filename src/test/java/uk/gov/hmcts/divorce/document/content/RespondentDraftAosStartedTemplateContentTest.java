package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
class RespondentDraftAosStartedTemplateContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private RespondentDraftAosStartedTemplateContent respondentDraftAosStartedTemplateConten;

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .emailAddress("contactdivorce@justice.gov.uk")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .phoneNumber("0300 303 0642")
            .build();

    @Test
    void shouldBeAbleToHandleRespondentDraftAosStartedTemplate() {
        assertThat(respondentDraftAosStartedTemplateConten.getSupportedTemplates())
                .containsAll(List.of(RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID));
    }

    @Test
    void shouldMapTemplateContentForApplicant1() {
        var caseData = TestDataHelper.caseData();
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder().addressLine1("line 1\ntown\npostcode").build());

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant1()
                .getLanguagePreference())).thenReturn("Wife");
        final Map<String, Object> templateContent = respondentDraftAosStartedTemplateConten
                .getTemplateContent(caseData, TEST_CASE_ID, caseData.getApplicant1());

        assertThat(templateContent).contains(
                entry(NAME, "test_first_name test_middle_name test_last_name"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID),
                entry(ADDRESS, "line 1\ntown\npostcode"),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT),
                entry(PARTNER, "Wife")
        );
    }
}
