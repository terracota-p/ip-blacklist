FROM adoptopenjdk:11-jre-openj9-bionic

RUN mkdir /opt/app
COPY target/anomaly-detections-ip-0.0.0.jar /opt/app/app.jar

RUN apt-get update \
    && apt-get install -y cron

# Bundle updated netsets at build time, they will be used by the app until first run of ipsets-update-and-reload.sh by cron
RUN mkdir /opt/blocklist-ipsets \
    && curl https://raw.githubusercontent.com/firehol/blocklist-ipsets/master/firehol_level1.netset > /opt/blocklist-ipsets/firehol_level1.netset \
    && curl https://raw.githubusercontent.com/firehol/blocklist-ipsets/master/firehol_level2.netset > /opt/blocklist-ipsets/firehol_level2.netset
ENV NETSETPATH /opt/blocklist-ipsets/firehol_level1.netset,/opt/blocklist-ipsets/firehol_level2.netset

# Setup cron reload script
COPY ipsets-update-and-reload.sh /opt/blocklist-ipsets/
COPY crontab /etc/cron.d/ipsets-update-and-reload-cron
RUN crontab /etc/cron.d/ipsets-update-and-reload-cron

EXPOSE 8080

COPY start.sh ./start.sh
ENTRYPOINT ["./start.sh"]
