require('ts-node/register');

const { testConfig } = require('./testConfig');

exports.config = {
  tests: testConfig.TestPathToRun,
  timeout: 20000,
  output: testConfig.TestOutputDir,
  helpers: {
    Puppeteer: {
      url: testConfig.TestUrl,
      show: testConfig.TestShowBrowserWindow,
      waitForNavigation: ['domcontentloaded'],
      restart: true,
      keepCookies: false,
      keepBrowserState: false,
      waitForTimeout: 90000,
      chrome: {
        ignoreHTTPSErrors: true,
        args: [
          //'--headless',
          '--disable-gpu',
          '--no-sandbox',
          '--allow-running-insecure-content',
          '--ignore-certificate-errors',
          '--window-size=1920,1080'
        ]
      }
    },
    PuppeteerHelper: {
      'require': './helper/PuppeteerHelper.js'
    },
    JSWait: {
      require: './helper/JSWait.js'
    },
//    Mochawesome: {
//      uniqueScreenshotNames: 'true'
//    }
  },
  include: {
    I: './steps.js'
  },
  plugins: {
     retryFailedStep: {
       enabled: true
     },
     autoDelay: {
       enabled: testConfig.TestAutoDelayEnabled
     },
     screenshotOnFail: {
       enabled: true,
       fullPageScreenshots: 'true'
     }
  },
  bootstrap: false,
  multiple: {
    'parallel': {
      'chunks': 2
    }
  },
//  mocha: {
//    reporterOptions: {
//      reportDir: testConfig.TestOutputDir,
//      reportName: 'Divorce CCD E2E Tests',
//      inlineAssets: true
//    }
//  },
  name: 'nfdiv-ccd-e2e-test'
};


