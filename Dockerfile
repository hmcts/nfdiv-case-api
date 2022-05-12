ARG APP_INSIGHTS_AGENT_VERSION=3.2.6
# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/nfdiv-case-api.jar /opt/app/
COPY lib/applicationinsights.json /opt/app/

EXPOSE 8489
CMD [ "nfdiv-case-api.jar" ]
