FROM adoptopenjdk:11-jre-openj9-bionic

RUN mkdir /opt/app
COPY target/anomaly-detections-ip-0.0.0.jar /opt/app/app.jar

RUN apt-get update \
    && apt-get install -y git \
    && apt-get install -y cron

# Bundle updated netsets at build time, as well as install the repo at /opt/blocklist-ipsets/ to enable reload to do `git pull`in it:
RUN mkdir /opt/blocklist-ipsets \
    && git clone https://github.com/firehol/blocklist-ipsets.git /opt/blocklist-ipsets/
# TODO for update-ipsets, it would be instead:
# RUN apt-get install -y zlib1g-dev gcc make git autoconf autogen automake pkg-config curl ipset \
#   && ...
ENV NETSETPATH /opt/blocklist-ipsets/firehol_level1.netset,/opt/blocklist-ipsets/firehol_level2.netset

# setup cron reload script
COPY ipsets-update-and-reload.sh /opt/blocklist-ipsets/
COPY crontab /etc/cron.d/ipsets-update-and-reload-cron
# TODO needed?:
# Give execution rights on the cron job
RUN chmod 0644 /etc/cron.d/ipsets-update-and-reload-cron
# Apply cron job
RUN crontab /etc/cron.d/ipsets-update-and-reload-cron

EXPOSE 8080

COPY start.sh ./start.sh
ENTRYPOINT ["./start.sh"]
