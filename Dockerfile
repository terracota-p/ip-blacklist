FROM adoptopenjdk:11-jre-openj9-bionic

RUN mkdir /opt/app
COPY target/anomaly-detections-ip-0.0.0.jar /opt/app/app.jar

RUN apt-get update \
    && apt-get install -y cron

# Bundle updated level1 and empty level2 netsets at build time (basic blacklisting without the false positives an outdated level2 netset could produce).
# They can be used by the app if for some reason the update-ipsets on startup does not work (see start.sh).
RUN mkdir /opt/blocklist-ipsets \
    && curl https://raw.githubusercontent.com/firehol/blocklist-ipsets/master/firehol_level1.netset > /opt/blocklist-ipsets/firehol_level1.netset \
    && touch /opt/blocklist-ipsets/firehol_level2.netset
ENV NETSETPATH /opt/blocklist-ipsets/firehol_level1.netset,/opt/blocklist-ipsets/firehol_level2.netset

# Install FireHOL's update-ipsets
COPY install-update-ipsets.sh /opt/update-ipsets/
RUN /opt/update-ipsets/install-update-ipsets.sh

# Setup cron reload script
COPY ipsets-update-and-reload.sh /opt/blocklist-ipsets/
COPY crontab /etc/cron.d/ipsets-update-and-reload-cron
RUN crontab /etc/cron.d/ipsets-update-and-reload-cron

EXPOSE 8080

COPY start.sh ./start.sh
ENTRYPOINT ["./start.sh"]
