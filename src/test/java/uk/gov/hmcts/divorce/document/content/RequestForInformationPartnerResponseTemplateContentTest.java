package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.document.content.templatecontent.RequestForInformationPartnerResponseTemplateContent;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_PARTNER_RESPONSE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_PARTNER_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID;

@ExtendWith(MockitoExtension.class)
class RequestForInformationPartnerResponseTemplateContentTest {

    @InjectMocks
    private RequestForInformationPartnerResponseTemplateContent requestForInformationPartnerResponseTemplateContent;

    @Test
    void shouldGetSupportedTemplates() {
        assertThat(requestForInformationPartnerResponseTemplateContent.getSupportedTemplates()).contains(
            REQUEST_FOR_INFORMATION_PARTNER_RESPONSE_LETTER_TEMPLATE_ID,
            REQUEST_FOR_INFORMATION_PARTNER_SOLICITOR_RESPONSE_LETTER_TEMPLATE_ID
        );
    }
}
