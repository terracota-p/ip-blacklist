#!/bin/sh

update-ipsets
curl -X POST localhost:8080/reload
