#!/usr/bin/env node

const { statSync } = require("fs");
const { readdir, mkdir, writeFile, readFile } = require("fs").promises;
const { resolve } = require('path');
const nfdivNamespace = "/k8s/namespaces/nfdiv/";

async function getFiles(dir) {
  const dirents = await readdir(dir, { withFileTypes: true });
  const files = await Promise.all(dirents.map((dirent) => {
    const res = resolve(dir, dirent.name);
    return dirent.isDirectory() ? getFiles(res) : res;
  }));
  return files.flat();
}

function getCronName(taskName) {
  return "nfdiv-cron-" + taskName.match(/[A-Z0-9][a-z0-9]+/g).filter(part => part !== "Task" && part !== "System").join("-").toLowerCase();
}

function getClusterOverride(taskName, cronName, schedule, env) {
  return `apiVersion: helm.fluxcd.io/v1
kind: HelmRelease
metadata:
  name: ${cronName}
spec:
  releaseName: ${cronName}
  values:
    global:
      jobKind: CronJob
      enableKeyVaults: true
      tenantId: "531ff96d-0ae9-462a-8d2d-bec7c0b42082"
      environment: ${env}
`
}

function getChartConfig(taskName, cronName, schedule) {
  return `apiVersion: helm.fluxcd.io/v1
kind: HelmRelease
metadata:
  name: ${cronName}
spec:
  releaseName: ${cronName}
  chart:
    git: git@github.com:hmcts/nfdiv-cron
    ref: 0.0.8
    path: nfdiv-cron
  values:
    job:
      image: hmctspublic.azurecr.io/nfdiv/case-api:prod-d25e51a-20210922052840 #{"$imagepolicy": "flux-system:nfdiv-case-api"}
      keyVaults:
        nfdiv:
          secrets:
            - name: AppInsightsInstrumentationKey
              alias: APP_INSIGHTS_KEY
            - name: uk-gov-notify-api-key
              alias: UK_GOV_NOTIFY_API_KEY
            - name: s2s-case-api-secret
              alias: S2S_SECRET
            - name: idam-secret
              alias: IDAM_CLIENT_SECRET
            - name: idam-systemupdate-username
              alias: IDAM_SYSTEM_UPDATE_USERNAME
            - name: idam-systemupdate-password
              alias: IDAM_SYSTEM_UPDATE_PASSWORD
      environment:
        TASK_NAME: ${taskName}
      schedule: ${schedule}
`
}

async function main(taskName, cnpFluxPath, schedule, env) {
  const cnpFluxStats = statSync(cnpFluxPath);

  if (!cnpFluxStats.isDirectory()) {
    console.error(cnpFluxPath + " is not a directory");
    process.exit(-1);
  }

  const clusterOverlayPath = `/k8s/${env}/cluster-00-overlay/nfdiv/kustomization.yaml`;

  let clusterOverlay;
  try {
    clusterOverlay = await readFile(cnpFluxPath + clusterOverlayPath, "utf8");
  } catch (err) {
    console.error(`Cannot find ${clusterOverlayPath} in ${cnpFluxPath}`);
    process.exit(-1);
  }

  const javaHome = __dirname + "/../src/main/java";
  const files = await getFiles(javaHome);

  if (!files.some(fileName => fileName.includes(taskName + ".java"))) {
    console.error(`Cannot find ${taskName}.java in ${javaHome}`);
    process.exit(-1);
  }

  const cronName = getCronName(taskName);
  const cronDirectory = cnpFluxPath + nfdivNamespace + cronName + "/";

  try {
    await mkdir(cronDirectory);
  } catch (err) {
    if (err.errno !== -17) {
      console.error(err);
    }
  }

  const clusterOverride = getClusterOverride(taskName, cronName, schedule, env);
  const chartConfig = getChartConfig(taskName, cronName, schedule);

  await Promise.all([
    writeFile(cronDirectory + `${env}-00.yaml`, clusterOverride),
    writeFile(cronDirectory + cronName + ".yaml", chartConfig),
  ]);

  const updatedOverlay = clusterOverlay
                          .replace("bases:", "bases:\n" + `- ../../../namespaces/nfdiv/${cronName}/${cronName}.yaml`)
                          .replace("patchesStrategicMerge:", "patchesStrategicMerge:\n" + `- ../../../namespaces/nfdiv/${cronName}/${env}-00.yaml`);

  writeFile(cnpFluxPath + clusterOverlayPath, updatedOverlay);
  console.log(`Added ${taskName} to cnp-flux-config.`);
}

const [exec, scriptPath, taskName, cnpFluxPath, schedule] = process.argv;

if (taskName && cnpFluxPath && schedule) {
  for (const env of ["aat", "ithc", "perftest", "demo"]) {
    main(taskName, cnpFluxPath, schedule, env).catch(e => console.error(e));
  }
} else {
  console.log(`
Usage: ./bin/add-cron.sh [taskName] [cnpFluxPath] [schedule]
Example: ./bin/add-cron.sh SystemProgressHeldCases ~/cnp-flux-config "0/10 * * * *"

Please note the quotes around the schedule.
`);
}
