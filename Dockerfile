ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/nfdiv-case-api.jar /opt/app/

EXPOSE 4013
CMD [ "nfdiv-case-api.jar" ]
