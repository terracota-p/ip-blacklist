#!/bin/sh

curl https://raw.githubusercontent.com/firehol/blocklist-ipsets/master/firehol_level1.netset > /opt/blocklist-ipsets/firehol_level1.netset
curl https://raw.githubusercontent.com/firehol/blocklist-ipsets/master/firehol_level2.netset > /opt/blocklist-ipsets/firehol_level2.netset
