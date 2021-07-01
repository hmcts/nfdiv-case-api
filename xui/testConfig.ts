import { execSync } from 'child_process';

function getSecret(secret: string): string {
  const result = execSync('az keyvault secret show --vault-name nfdiv-aat -o tsv --query value --name ' + secret);

  return result.toString().replace('\n', '');
}

export const testConfig = {
  TestUrl: process.env.TEST_E2E_URL || 'https://manage-case.aat.platform.hmcts.net/',
  TestEnv: process.env.RUNNING_ENV || 'aat',
  TestShowBrowserWindow: process.env.SHOW_BROWSER_WINDOW || true,
  TestRetryFeatures: 0,
  TestRetryScenarios: +(process.env.RETRY_SCENARIOS || 0),
  TestPathToRun: process.env.E2E_TEST_PATH || './**/*.test.ts',
  TestOutputDir: process.env.E2E_OUTPUT_DIR || '../functional-output/',
  TestEnvSolUser: 'TEST_SOLICITOR@mailinator.com',
  TestEnvSolPassword: getSecret('idam-solicitor-password'),
  TestEnvCWUser: 'TEST_CASE_WORKER_USER@mailinator.com',
  TestEnvCWPassword: getSecret('idam-caseworker-password'),
  TestEnvCourtAdminUser:'DivCaseWorkerUser@AAT.com',
  TestEnvCourtAdminPassword: getSecret('idam-caseworker-password'),
  TestEnvProfUser: process.env.PROF_USER_EMAIL || '',
  TestEnvProfPassword: process.env.PROF_USER_PASSWORD || '',
  TestForXUI: process.env.TESTS_FOR_XUI_SERVICE === 'true',
  TestForAccessibility: process.env.TESTS_FOR_ACCESSIBILITY === 'true',
  TestForCrossBrowser: process.env.TESTS_FOR_CROSS_BROWSER === 'true',
  TestIdamClientSecret: process.env.IDAM_CLIENT_SECRET || getSecret('idam-secret'),
  TestS2SAuthSecret: process.env.SERVICE_SECRET || getSecret('frontend-secret')
};
