package uk.gov.hmcts.divorce.document.content;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;

import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_JUSTICE_GOV_UK_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;

@ExtendWith(MockitoExtension.class)
class DocmosisCommonContentTest {

    @InjectMocks
    private DocmosisCommonContent docmosisCommonContent;

    @Test
    void shouldReturnEnglishTemplateContentForEnglish() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(NO).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        Assertions.assertThat(templateContent).isNotEmpty().hasSize(4)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                entry(CONTACT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT));
    }

    @Test
    void shouldReturnWelshTemplateContentForWelsh() {

        Applicant applicant = Applicant.builder().languagePreferenceWelsh(YES).build();

        var templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        Assertions.assertThat(templateContent).isNotEmpty().hasSize(4)
            .contains(
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY),
                entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY),
                entry(CONTACT_EMAIL, CONTACT_JUSTICE_GOV_UK_CY),
                entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY));
    }
}
