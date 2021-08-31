#!/usr/bin/env node

const { statSync } = require("fs");
const { readdir, mkdir, writeFile, readFile } = require("fs").promises;
const { resolve } = require('path');
const nfdivNamespace = "/k8s/namespaces/nfdiv/";
const aatClusterOverlayPath = "/k8s/aat/cluster-00-overlay/nfdiv/kustomization.yaml";

async function getFiles(dir) {
  const dirents = await readdir(dir, { withFileTypes: true });
  const files = await Promise.all(dirents.map((dirent) => {
    const res = resolve(dir, dirent.name);
    return dirent.isDirectory() ? getFiles(res) : res;
  }));
  return files.flat();
}

function getCronName(taskName) {
  return "nfdiv-" + taskName.match(/[A-Z][a-z]+/g).filter(part => part != "task").join("-").toLowerCase();
}

function getClusterOverride(taskName, cronName, schedule) {
  return `apiVersion: helm.fluxcd.io/v1
kind: HelmRelease
metadata:
  name: ${cronName}
spec:
  values:
    job:
      args:
        - run
        - ${taskName}
      schedule: ${schedule}
    global:
      jobKind: CronJob
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
    ref: 0.0.6
    path: nfdiv-cron
  values:
    job:
      image: hmctspublic.azurecr.io/nfdiv/case-api:prod-00fe383-20210826060439
      args:
        - run
        - ${taskName}
      schedule: ${schedule}
`
}

async function main(taskName, cnpFluxPath, schedule) {
  const cnpFluxStats = statSync(cnpFluxPath);

  if (!cnpFluxStats.isDirectory()) {
    console.error(cnpFluxPath + " is not a directory");
    process.exit(-1);
  }

  let aatClusterOverlay;
  try {
    aatClusterOverlay = await readFile(cnpFluxPath + aatClusterOverlayPath, "utf8");
  } catch (err) {
    console.error(`Cannot find ${aatClusterOverlayPath} in ${cnpFluxPath}`);
    process.exit(-1);
  }

  const javaHome = __dirname + "/../src/main/java";
  const files = await getFiles(javaHome);

  if (!files.some(fileName => fileName.includes(taskName + ".java"))) {
    console.error(`Cannot find ${taskName}.java in ${javaHome}`);
    process.exit(-1);
  }

  const cronName = getCronName(taskName);
  const cronDirectory = cnpFluxPath + nfdivNamespace + cronName;

  try {
    await mkdir(cronDirectory);
  } catch (err) {
    if (err.errno !== -17) {
      console.error(err);
    }
  }

  const clusterOverride = getClusterOverride(taskName, cronName, schedule);
  const chartConfig = getChartConfig(taskName, cronName, schedule);

  await Promise.all([
    writeFile(cronDirectory + "aat-00.yaml", clusterOverride),
    writeFile(cronDirectory + cronName + ".yaml", chartConfig),
  ]);

  const updatedOverlay = aatClusterOverlay
                          .replace("bases:", "bases:\n" + `- ../../../namespaces/nfdiv/${cronName}/${cronName}.yaml`)
                          .replace("patchesStrategicMerge:", "patchesStrategicMerge:\n" + `- ../../../namespaces/nfdiv/${cronName}/aat-00.yaml`);

  writeFile(cnpFluxPath + aatClusterOverlayPath, updatedOverlay);
  console.log(`Added ${taskName} to cnp-flux-config.`);
}

const [exec, scriptPath, taskName, cnpFluxPath, schedule] = process.argv;

if (taskName && cnpFluxPath && schedule) {
  main(taskName, cnpFluxPath, schedule).catch(e => console.error(e));
} else {
  console.log(`
Usage: ./bin/add-cron.sh [taskName] [cnpFluxPath] [schedule]
Example: ./bin/add-cron.sh SystemProgressHeldCases ~/cnp-flux-config "0/10 * * * *"

Please note the quotes around the schedule.
`);
}
