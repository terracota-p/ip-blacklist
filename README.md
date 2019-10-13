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

