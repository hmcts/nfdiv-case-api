ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.2

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/nfdiv-case-api.jar /opt/app/

EXPOSE 4013
CMD [ "nfdiv-case-api.jar" ]
