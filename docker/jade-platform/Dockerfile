FROM eclipse-temurin:17-jre

ENV JADE_HOME=/opt/jade
WORKDIR ${JADE_HOME}

# Copy vendored JADE libs from build context
COPY vendor/jade/lib /opt/jade/lib

# Copy entrypoint
COPY jade-platform/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 1099
ENTRYPOINT ["/entrypoint.sh"]
