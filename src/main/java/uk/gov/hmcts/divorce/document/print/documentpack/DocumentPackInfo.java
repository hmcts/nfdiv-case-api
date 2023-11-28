package uk.gov.hmcts.divorce.document.print.documentpack;

import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Map;
import java.util.Optional;

public record DocumentPackInfo(Map<DocumentType, Optional<String>> documentPack, Map<String, String> templateInfo) {}
