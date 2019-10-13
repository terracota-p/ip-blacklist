#!/bin/sh

/opt/blocklist-ipsets/ipsets-update.sh
curl -X POST localhost:8080/reload
