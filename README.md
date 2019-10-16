# anomaly-detections-ip

Service to check if IP is blacklisted.

## Build

Jar:

```shell script
mvn clean install
```

Docker image:

```shell script
docker build . -t anomaly-detections-ip:latest
```

## Run docker

```shell script
docker run --name anomaly-detections-ip -p 8080:8080 -d anomaly-detections-ip:latest
# See logs:
docker logs anomaly-detections-ip -f
```

## Test

```shell script
# Should return 204 (negative)
curl -i http://localhost:8080/ips/1.1.1.1

# Should return 200 (positive: IP is blacklisted)
curl -i http://localhost:8080/ips/0.0.0.0

# Optionally, if you want to reload:
curl -X POST http://localhost:8080/reload
```

## Alternatively - Run as standalone

Eg:

```shell script
export NETSETPATH=./src/test/resources/firehol_level1.netset,./src/test/resources/firehol_level2.netset \
  && java -jar target/anomaly-detections-ip-0.0.0.jar
```

## Acceptance tests

See [acceptance-tests](./acceptance-tests/README.md)

## Load tests

See [load-tests](./load-tests/README.md)

## Test UI

See [test-ui](./test-ui/README.md)

## Possible improvements - Plan for production release

Before going to prod, we should think about:
- Deployment - For instance we could use k8s, then create the configmaps
- Horizontal scalability, high availability - Deploy 3 instances at least, with a load-balancer at the front (eg: you can do that with an nginx ingress in k8s)
- Frequent update interval - Eg: cron every 5 mins: update-ipsets downloads from maintainers (that update frequently, as opposed to the git repo that updates once a day), and can be run as frequently as we want (each list has a max frequency allowed and it respects it, and also only downloads if the file has updates). So, we could potentially run it with cron eg every 5 mins (useful to get quick updates of current attacks), instead of the current once-a-day setup.
- What to log? - Define if any logging would be useful (and re-run load tests)
- Separate container for the update-ipsets.sh that stores files in shared volume (*) / cloud storage. Then, the app service could read them from that volume instead of file within docker container.
	- (*) eg: we could setup a k8s multi-container pod, where both containers (updater and service) have access to shared volume - https://kubernetes.io/docs/concepts/workloads/pods/pod/
- Desirable User Story - Whitelisting
- Desirable User Story - Allow request param for the desired levels to match against - and have an `IpSet` by level, iterate over them for match
- Desirable User Story? - Support IPv6 blacklists (currently only IPv4 blacklists are supported by the service, note blocklist-ipsets repo only contains IPv4 lists)
- ? further load tests, add request limiter in the service if necessary, that could return HTTP status code 429 (Too Many Requests) when overwhelmed
