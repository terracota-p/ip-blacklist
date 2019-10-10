# anomaly-detections-ip

Service to check if IP is blacklisted.

## Build

```shell script
mvn clean install
```

## Run

```shell script
export NETSETPATH=../blocklist-ipsets/firehol_level1.netset && java -jar target/anomaly-detections-ip-0.0.0.jar
```

## Test

```shell script
curl -X POST http://localhost:8080/reload

# Should return 204 (negative)
curl -i http://localhost:8080/ips/1.1.1.1

# Should return 200 (positive: IP is blacklisted)
curl -i http://localhost:8080/ips/0.0.0.0
```