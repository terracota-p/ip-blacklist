#!/bin/sh

/opt/blocklist-ipsets/ipsets-update.sh
cron && java -jar /opt/app/app.jar