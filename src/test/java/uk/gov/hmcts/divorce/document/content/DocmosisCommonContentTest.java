package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_JUSTICE_GOV_UK_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;

@ExtendWith(MockitoExtension.class)
class DocmosisCommonContentTest {

    private static final String PO_BOX = "PO Box 13226";
    private static final String TOWN = "Harlow";
    private static final String POSTCODE = "CM20 9UG";

    private static final int EXPECTED_ENTRY_SIZE = 5;

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails
        .builder()
        .poBox(PO_BOX)
        .town(TOWN)
        .postcode(POSTCODE)
        .build();

    @InjectMocks
    private DocmosisCommonContent docmosisCommonContent;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(docmosisCommonContent, "poBox", PO_BOX);
        ReflectionTestUtils.setField(docmosisCommonContent, "town", TOWN);
        ReflectionTestUtils.setField(docmosisCommonContent, "postcode", POSTCODE);
    }

    @Test
    void shouldReturnEnglishTemplateContentForEnglish() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(NO).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT));
    }

    @Test
    void shouldReturnWelshTemplateContentForWelsh() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(YES).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        assertThat(templateContent).isNotEmpty().hasSize(EXPECTED_ENTRY_SIZE)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
                entry(CONTACT_EMAIL, CONTACT_JUSTICE_GOV_UK_CY),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY),
                entry(CTSC_CONTACT_DETAILS, CTSC_CONTACT));
    }
}
