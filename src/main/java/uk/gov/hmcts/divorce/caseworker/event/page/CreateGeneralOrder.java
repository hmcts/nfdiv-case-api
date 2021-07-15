package uk.gov.hmcts.divorce.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.GeneralOrderTemplateContent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_ORDER;

@Slf4j
@Component
public class CreateGeneralOrder implements CcdPageConfiguration {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private GeneralOrderTemplateContent generalOrderTemplateContent;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private Clock clock;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("createGeneralOrder", this::midEvent)
            .complex(CaseData::getGeneralOrder)
            .mandatory(GeneralOrder::getGeneralOrderDate)
            .mandatory(GeneralOrder::getGeneralOrderDivorceParties)
            .optional(GeneralOrder::getGeneralOrderRecitals)
            .mandatory(GeneralOrder::getGeneralOrderJudgeType)
            .mandatory(GeneralOrder::getGeneralOrderJudgeName)
            .mandatory(GeneralOrder::getGeneralOrderDetails)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for CreateGeneralOrder");

        var caseDataCopy = details.getData().toBuilder().build();
        var generalOrder = caseDataCopy.getGeneralOrder();

        final Long caseId = details.getId();

        final Supplier<Map<String, Object>> templateContentSupplier = generalOrderTemplateContent
            .apply(caseDataCopy, caseId);

        log.info("Generating general order document for templateId : {} case caseId: {}", DIVORCE_GENERAL_ORDER, caseId);

        Document generalOrderDocument = caseDataDocumentService.renderGeneralOrderDocument(
            templateContentSupplier,
            null,// else case caseId will be appended to document name
            httpServletRequest.getHeader(AUTHORIZATION),
            DIVORCE_GENERAL_ORDER,
            GENERAL_ORDER + LocalDateTime.now(clock).format(formatter),
            caseDataCopy.getApplicant1().getLanguagePreference()
        );

        generalOrder.setGeneralOrderDraft(generalOrderDocument);
        caseDataCopy.setGeneralOrder(generalOrder);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .build();
    }
}
