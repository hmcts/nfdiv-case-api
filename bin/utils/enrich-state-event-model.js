#!/usr/bin/env node

/**
 * Enriches the state-event model by resolving dynamic (wildcard) post-states.
 *
 * Parses nfdiv-case-api Java callback handlers and associated tasks to find
 * which CaseState values each event can transition to via .state() or .setState() calls.
 * This replaces '*' with the actual concrete state(s) discovered in the source code.
 *
 * Usage:
 *   node bin/utils/enrich-state-event-model.js [model.json] [api-service-path]
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');

const VALID_STATES = new Set([
  'AosDrafted','AosOverdue','Applicant2Approved','Archived','AwaitingAdminClarification',
  'AwaitingAlternativeService','AwaitingAmendedApplication','AwaitingAnswer','AwaitingAos',
  'AwaitingApplicant1Response','AwaitingApplicant2Response','AwaitingBailiffReferral',
  'AwaitingBailiffService','AwaitingClarification','AwaitingConditionalOrder','AwaitingDocuments',
  'AwaitingDwpResponse','AwaitingFinalOrder','AwaitingFinalOrderPayment','AwaitingGenAppHWFEvidence',
  'AwaitingGenAppHWFPartPayment','AwaitingGeneralApplicationPayment','AwaitingGeneralConsideration',
  'AwaitingGeneralReferralPayment','AwaitingHWFDecision','AwaitingHWFEvidence','AwaitingHWFPartPayment',
  'AwaitingJointFinalOrder','AwaitingJsNullity','AwaitingJudgeClarification','AwaitingLegalAdvisorReferral',
  'AwaitingPayment','AwaitingPronouncement','AwaitingRequestedInformation','AwaitingResponseToHWFDecision',
  'AwaitingService','AwaitingServiceConsideration','AwaitingServicePayment','BailiffRefused',
  'BulkCaseReject','ClarificationSubmitted','ConditionalOrderDrafted','ConditionalOrderPending',
  'ConditionalOrderPronounced','ConditionalOrderRefused','ConditionalOrderReview','Draft',
  'FinalOrderComplete','FinalOrderPending','FinalOrderRequested','GeneralApplicationReceived',
  'GeneralConsiderationComplete','Holding','InBulkActionCase','InformationRequested','IssuedToBailiff',
  'JSAwaitingLA','LAReview','LAServiceReview','NewPaperCase','OfflineDocumentReceived','PendingHearingDate','PendingHearingOutcome',
  'PendingRefund','PendingServiceAppResponse','Rejected','RequestedInformationSubmitted',
  'RespondentFinalOrderRequested','SeparationOrderGranted','ServiceAdminRefusal','Submitted',
  'WelshTranslationRequested','WelshTranslationReview','Withdrawn',
]);

function walkJava(dir) {
  const results = [];
  if (!fs.existsSync(dir)) return results;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) results.push(...walkJava(full));
    else if (entry.name.endsWith('.java')) results.push(full);
  }
  return results;
}

/**
 * Extract states from .state(...) or .setState(...) call sites in a Java file.
 * Handles:
 *   .state(State.X)
 *   .state(State.X.name())
 *   .state("STATE_STRING")
 *   .setState(State.X)
 *   .state(variable) — traces variable assignment in same file
 */
function extractStatesFromStateCalls(filePath) {
  const content = fs.readFileSync(filePath, 'utf8');
  const states = new Set();

  // Build static import map: short name -> full enum value
  const imports = {};
  const importRe = /import static\s+.*State\.([A-Za-z0-9]+)\s*;/g;
  let m;
  while ((m = importRe.exec(content)) !== null) {
    imports[m[1]] = m[1];
  }

  // Find all .state(...) or .setState(...) call sites
  const stateCallRe = /\.?set?State\(([^)]+)\)/g;
  while ((m = stateCallRe.exec(content)) !== null) {
    const arg = m[1].trim();

    // Case 1: .state(State.ENUM) or .state(State.ENUM.name())
    const enumDirect = arg.match(/State\.([A-Za-z0-9]+)/);
    if (enumDirect && VALID_STATES.has(enumDirect[1])) {
      states.add(enumDirect[1]);
      continue;
    }

    // Case 2: .state("STATE_STRING")
    const stringLiteral = arg.match(/^"([A-Za-z0-9]+)"$/);
    if (stringLiteral && VALID_STATES.has(stringLiteral[1])) {
      states.add(stringLiteral[1]);
      continue;
    }

    // Case 3: .state(IMPORTED_ENUM) — static imported State
    const importedEnum = arg.match(/^([A-Za-z0-9]+)(\.name\(\))?$/);
    if (importedEnum && imports[importedEnum[1]] && VALID_STATES.has(importedEnum[1])) {
      states.add(importedEnum[1]);
      continue;
    }

    // Case 4: .state(variable) — trace the variable assignment
    const varMatch = arg.match(/^([a-zA-Z_]\w*)$/);
    if (varMatch) {
      const varName = varMatch[1];
      traceVariable(content, varName, imports).forEach(s => states.add(s));
      continue;
    }

    // Case 5: Ternary or complex expression containing State references
    const allEnums = arg.match(/State\.([A-Za-z0-9]+)/g);
    if (allEnums) {
      allEnums.forEach(ref => {
        const s = ref.replace('State.', '');
        if (VALID_STATES.has(s)) states.add(s);
      });
      continue;
    }

    // Case 6: Ternary with static imports
    for (const [shortName, fullName] of Object.entries(imports)) {
      if (arg.includes(shortName) && VALID_STATES.has(fullName)) {
        states.add(fullName);
      }
    }
  }

  return states;
}

/**
 * Trace a variable back to its State assignment(s) within the same file.
 */
function traceVariable(content, varName, imports) {
  const states = new Set();
  const escaped = varName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

  // Pattern: varName = State.X
  const assignRe = new RegExp(`${escaped}\\s*=\\s*([^;]+);`, 'g');
  let m;
  while ((m = assignRe.exec(content)) !== null) {
    const rhs = m[1].trim();

    // State.X
    const enumRef = rhs.match(/State\.([A-Za-z0-9]+)/);
    if (enumRef && VALID_STATES.has(enumRef[1])) {
      states.add(enumRef[1]);
      continue;
    }

    // "STATE_STRING"
    const strRef = rhs.match(/^"([A-Za-z0-9]+)"$/);
    if (strRef && VALID_STATES.has(strRef[1])) {
      states.add(strRef[1]);
      continue;
    }

    // Static imported enum: CASE_SETTLED
    const impRef = rhs.match(/^([A-Za-z0-9]+)(\.name\(\))?$/);
    if (impRef && imports[impRef[1]] && VALID_STATES.has(impRef[1])) {
      states.add(impRef[1]);
      continue;
    }

    // Ternary with State refs
    const ternaryRefs = rhs.match(/State\.([A-Za-z0-9]+)/g);
    if (ternaryRefs) {
      ternaryRefs.forEach(ref => {
        const s = ref.replace('State.', '');
        if (VALID_STATES.has(s)) states.add(s);
      });
    }

    // Ternary with imported enums
    for (const [shortName, fullName] of Object.entries(imports)) {
      if (rhs.includes(shortName) && VALID_STATES.has(fullName)) {
        states.add(fullName);
      }
    }

    // Method call — try to trace into same file
    const methodCall = rhs.match(/^(\w+)\(/);
    if (methodCall) {
      traceMethod(content, methodCall[1], imports).forEach(s => states.add(s));
    }
  }

  return states;
}

/**
 * Trace a method's return statements for State references.
 */
function traceMethod(content, methodName, imports) {
  const states = new Set();
  // Find the method body (simplified — look for return statements after the method signature)
  const escaped = methodName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const methodRe = new RegExp(`\\b${escaped}\\s*\\([^)]*\\)\\s*\\{`, 'g');
  const match = methodRe.exec(content);
  if (!match) return states;

  // Extract a chunk after the method signature
  const bodyStart = match.index + match[0].length;
  const bodyChunk = content.substring(bodyStart, bodyStart + 2000);

  // Find return statements with State
  const returnRe = /return\s+([^;]+);/g;
  let m;
  while ((m = returnRe.exec(bodyChunk)) !== null) {
    const ret = m[1];
    const refs = ret.match(/State\.([A-Za-z0-9]+)/g);
    if (refs) refs.forEach(r => {
      const s = r.replace('State.', '');
      if (VALID_STATES.has(s)) states.add(s);
    });
    // Static imported
    for (const [shortName, fullName] of Object.entries(imports)) {
      if (ret.includes(shortName) && VALID_STATES.has(fullName)) {
        states.add(fullName);
      }
    }
    // String literal
    const strRef = ret.match(/"([A-Za-z0-9]+)"/);
    if (strRef && VALID_STATES.has(strRef[1])) states.add(strRef[1]);
  }

  return states;
}

/**
 * Find handler files for a given event ID.
 * Verifies the event ID appears as a distinct ID string.
 */
function findHandlersForEvent(eventId, allFiles) {
  const handlers = [];
  const escaped = eventId.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  // Match .event("eventId") or public static final String ID = "eventId"
  const eventRe = new RegExp(`(?:\\.event\\(\\s*"${escaped}"\\s*\\)|"${escaped}")`, 'm');

  for (const file of allFiles) {
    const content = fs.readFileSync(file, 'utf8');
    if (eventRe.test(content)) {
      handlers.push(file);
    }
  }
  return handlers;
}

/**
 * Find CaseTask files associated with a handler.
 * Looks for CaseTask implementations and where they are used.
 */
function findTaskFiles(handlerFile, allFiles, serviceRoot) {
  const content = fs.readFileSync(handlerFile, 'utf8');
  const tasks = new Set();

  // Find all CaseTask types used in the handler
  const taskRunnerRe = /taskRunner\.run\(\s*([^,)]+)/g;
  let m;
  while ((m = taskRunnerRe.exec(content)) !== null) {
    const taskVar = m[1].trim();
    // Find where taskVar is defined
    const varDefRe = new RegExp(`(?:private|public|final)\\s+([A-Z]\\w+)\\s+${taskVar}`, 'g');
    let m2;
    while ((m2 = varDefRe.exec(content)) !== null) {
      const className = m2[1];
      // Search for this class file in allFiles
      for (const f of allFiles) {
        if (f.endsWith('/' + className + '.java')) {
          tasks.add(f);
        }
      }
    }
  }

  // Also check imports for any classes from task packages
  const importRe = /import\s+(uk\.gov\.hmcts\.divorce\.(?:.*\.)?task\.[A-Z]\w+);/g;
  while ((m = importRe.exec(content)) !== null) {
    const fullPath = path.join(serviceRoot, 'src/main/java', m[1].replace(/\./g, '/') + '.java');
    if (fs.existsSync(fullPath)) {
      tasks.add(fullPath);
    }
  }

  return [...tasks];
}

function main() {
  const modelPath = process.argv[2]
    ? path.resolve(process.cwd(), process.argv[2])
    : path.join(ROOT, 'build', 'state-event-model.json');

  const serviceRoot = process.argv[3]
    ? path.resolve(process.cwd(), process.argv[3])
    : ROOT;

  if (!fs.existsSync(serviceRoot)) {
    console.error(`Service root not found at: ${serviceRoot}`);
    process.exit(1);
  }

  const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
  console.log(`Loaded model: ${model.events.length} events`);

  const handlerDir = path.join(serviceRoot, 'src/main/java/uk/gov/hmcts/divorce');
  const allFiles = walkJava(handlerDir);
  console.log(`Found ${allFiles.length} Java files`);

  const dynamicEvents = model.events.filter(
    e => e.postState === '*'
  );
  console.log(`Dynamic post-state events to resolve: ${dynamicEvents.length}`);

  let resolved = 0, noChange = 0, unresolved = 0;

  for (const ev of dynamicEvents) {
    const handlers = findHandlersForEvent(ev.id, allFiles);
    const allStates = new Set();

    for (const handlerFile of handlers) {
      // Extract states from .state() calls in handler
      extractStatesFromStateCalls(handlerFile).forEach(s => allStates.add(s));

      // Also check child task files
      const tasks = findTaskFiles(handlerFile, allFiles, serviceRoot);
      for (const tf of tasks) {
        extractStatesFromStateCalls(tf).forEach(s => allStates.add(s));
      }
    }

    if (allStates.size > 0) {
      ev.resolvedPostStates = [...allStates].sort();
      resolved++;
    } else if (handlers.length > 0) {
      // Handler exists but has no .state() call — event doesn't change state
      ev.resolvedPostStates = ['NO_CHANGE'];
      noChange++;
    } else {
      ev.resolvedPostStates = [];
      unresolved++;
    }
  }

  model.summary.resolvedDynamicEvents = resolved;
  model.summary.noChangeDynamicEvents = noChange;
  model.summary.unresolvedDynamicEvents = unresolved;
  model.enrichedAt = new Date().toISOString();
  model.enrichmentSource = path.basename(serviceRoot);

  fs.writeFileSync(modelPath, JSON.stringify(model, null, 2));
  console.log(`\nEnrichment complete:`);
  console.log(`  Resolved (state change): ${resolved}`);
  console.log(`  No state change: ${noChange}`);
  console.log(`  Unresolved (no handler found): ${unresolved}`);
  console.log(`  Written to: ${modelPath}`);
}

main();
