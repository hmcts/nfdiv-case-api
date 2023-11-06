package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Optional;

public record DocumentPackInfo(ImmutableMap<DocumentType, Optional<String>> documentPack, ImmutableMap<String, String> templateInfo) {}
