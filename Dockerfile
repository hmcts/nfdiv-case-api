# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.19

FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY build/libs/nfdiv-case-api.jar /opt/app/
COPY lib/applicationinsights.json /opt/app/

EXPOSE 4013
CMD [ "nfdiv-case-api.jar" ]
