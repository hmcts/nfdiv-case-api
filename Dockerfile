ARG APP_INSIGHTS_AGENT_VERSION=2.6.4

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/nfdiv-case-api.jar /opt/app/

EXPOSE 4013
CMD [ "nfdiv-case-api.jar" ]
