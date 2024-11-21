package uk.gov.hmcts.divorce.sow014.nfd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.jooq.DSLContext;
import static org.jooq.nfdiv.ccd.Tables.FAILED_JOBS;
import static org.jooq.nfdiv.public_.Tables.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.model.CaseNote;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.sow014.lib.CaseRepository;

@Component
public class NFDCaseRepository implements CaseRepository {

    @Autowired
    private DSLContext db;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PebbleEngine pebl;

    @SneakyThrows
    @Override
    public ObjectNode getCase(long caseRef, ObjectNode caseData) {
        var isLeadCase = db.fetchOptional(MULTIPLES, MULTIPLES.LEAD_CASE_ID.eq(caseRef));
        if (isLeadCase.isPresent()) {
            addLeadCaseInfo(caseRef, caseData);
        } else {
            caseData = addSubCaseInfo(caseRef, caseData);
        }

        var notes = loadNotes(caseRef);
        caseData.set("notes", mapper.valueToTree(notes));

        caseData.put("markdownTabField", renderExampleTab(caseRef, notes));

        caseData.put("hyphenatedCaseRef", CaseData.formatCaseRef(caseRef));

        addAdminPanel(caseRef, caseData);

        return caseData;
    }

    private void addAdminPanel(long caseRef, ObjectNode caseData) throws IOException {
        PebbleTemplate compiledTemplate = pebl.getTemplate("admin");
        Writer writer = new StringWriter();

        var failedJobs = db.fetch(FAILED_JOBS, FAILED_JOBS.REFERENCE.eq(caseRef));
        Map<String, Object> context = new HashMap<>();
        context.put("failedJobs", failedJobs);
        context.put("caseRef", caseRef);

        compiledTemplate.evaluate(writer, context);
        caseData.put("adminMd", writer.toString());
    }

    private void addLeadCaseInfo(long caseRef, ObjectNode caseData) throws IOException {
        // Fetch first 50
        var total = db.fetchCount(SUB_CASES, SUB_CASES.LEAD_CASE_ID.eq(caseRef));
        var subCases = db.selectFrom(SUB_CASES)
                .where(SUB_CASES.LEAD_CASE_ID.eq(caseRef))
                .limit(50)
                .fetch();
        if (subCases.isNotEmpty()) {
            caseData.put("leadCase", "Yes");

            PebbleTemplate compiledTemplate = pebl.getTemplate("subcases");
            Writer writer = new StringWriter();

            Map<String, Object> context = new HashMap<>();
            context.put("subcases", subCases);
            context.put("total", total);

            compiledTemplate.evaluate(writer, context);
            caseData.put("leadCaseMd", writer.toString());
        } else {
            caseData.put("leadCase", "No");
        }
    }

    private ObjectNode addSubCaseInfo(long caseRef, ObjectNode caseData) throws IOException {
        var leadCase = db.fetchOptional(SUB_CASES, SUB_CASES.SUB_CASE_ID.eq(caseRef));

        if (leadCase.isPresent()) {
            var derivedData = db.fetchOptional(DERIVED_CASES, DERIVED_CASES.SUB_CASE_ID.eq(caseRef));
            caseData = mapper.readValue(derivedData.get().getData().data(), ObjectNode.class);
            caseData.put("leadCase", "No");

            PebbleTemplate compiledTemplate = pebl.getTemplate("leadcase");
            Writer writer = new StringWriter();

            Map<String, Object> context = new HashMap<>();
            context.put("leadCase", leadCase.get());

            compiledTemplate.evaluate(writer, context);
            caseData.put("subCaseMd", writer.toString());
        }
        return caseData;
    }


    private List<ListValue<CaseNote>> loadNotes(long caseRef) {
        return db.select()
           .from(CASE_NOTES)
           .where(CASE_NOTES.REFERENCE.eq(caseRef))
           .orderBy(CASE_NOTES.DATE.desc())
           .fetchInto(CaseNote.class)
           .stream().map(n -> new ListValue<>(null, n))
           .toList();
    }

    @SneakyThrows
    private String renderExampleTab(long caseRef, List<ListValue<CaseNote>> notes) {
        PebbleTemplate compiledTemplate = pebl.getTemplate("notes");
        Writer writer = new StringWriter();

        long uptimeInSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        Map<String, Object> context = new HashMap<>();
        context.put("caseRef", caseRef);
        context.put("age", uptimeInSeconds);
        context.put("notes", notes);

        compiledTemplate.evaluate(writer, context);

        return writer.toString();
    }
}
