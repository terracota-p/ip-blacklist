# anomaly-detections-ip

Service to check if IP is blacklisted.

## Build

```shell script
mvn clean install
```

## Run

Eg:

```shell script
export NETSETPATH=./src/test/resources/firehol_level1.netset,./src/test/resources/firehol_level2.netset \
  && java -jar target/anomaly-detections-ip-0.0.0.jar
```

## Test

```shell script
curl -X POST http://localhost:8080/reload

# Should return 204 (negative)
curl -i http://localhost:8080/ips/1.1.1.1

# Should return 200 (positive: IP is blacklisted)
curl -i http://localhost:8080/ips/0.0.0.0
```