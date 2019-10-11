FROM adoptopenjdk:11-jre-openj9-bionic

RUN mkdir /opt/app
COPY target/anomaly-detections-ip-0.0.0.jar /opt/app/app.jar

# XXX We are bundling here the same netsets we use for unit tests.
# In a production-ready setup, CI build script should update-ipsets before building docker image.
COPY src/test/resources/firehol_level1.netset /opt/app/
COPY src/test/resources/firehol_level2.netset /opt/app/

EXPOSE 8080

ENV NETSETPATH /opt/app/firehol_level1.netset,/opt/app/firehol_level2.netset
CMD ["java", "-jar", "/opt/app/app.jar"]
