#!/bin/sh

cd /opt/blocklist-ipsets
git pull
curl -X POST localhost:8080/reload
