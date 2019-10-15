const async = require("async");
const rp = require("request-promise");
const randomip = require("random-ip");

const requests = 2000;
const urls = Array(requests)
  .fill(0)
  .map(value => "http://localhost:8080/ips/" + randomIp());
const overallStartTime = new Date().getTime();
async.mapLimit(
  urls,
  5,
  async function(url) {
    var response;
    var options = {
      method: "GET",
      uri: url,
      resolveWithFullResponse: true,
      time: true
    };
    return rp(options)
      .then(response => response)
      .catch(err => {
        throw err;
      });
  },
  (err, results) => {
    if (err) throw err;
    // results is now an array of the responses

    const overallTime = new Date().getTime() - overallStartTime;
    const requestsPerSecond = (requests * 1000) / overallTime;

    const {
      averageLatency,
      requestsWithUnexpectedStatus,
      requestsOverMaxThreshold,
      positives,
      negatives
    } = aggregateResults(results);

    printResultsSummary(
      averageLatency,
      results,
      requestsPerSecond,
      positives,
      negatives
    );

    checkResults(
      results,
      requestsWithUnexpectedStatus,
      requestsOverMaxThreshold,
      averageLatency,
      requestsPerSecond
    );
  }
);

function randomIp() {
  return randomip("0.0.0.0", 0);
}

function aggregateResults(results) {
  const requestsWithUnexpectedStatus = results.filter(
    response => response.statusCode !== 200 && response.statusCode !== 204
  );
  const requestsOverMaxThreshold = results.filter(
    response => response.elapsedTime > 200
  );
  const averageLatency =
    results
      .map(response => response.elapsedTime)
      .reduce((accumulator, value) => accumulator + value) / requests;
  const positives = results.filter(response => response.statusCode === 200)
    .length;
  const negatives = results.filter(response => response.statusCode === 204)
    .length;
  return {
    averageLatency,
    requestsWithUnexpectedStatus,
    requestsOverMaxThreshold,
    positives,
    negatives
  };
}

function printResultsSummary(
  averageLatency,
  results,
  requestsPerSecond,
  positives,
  negatives
) {
  console.log("average latency: " + averageLatency + "ms");
  console.log(
    "max latency: " +
      results
        .map(response => response.elapsedTime)
        .reduce((accumulator, value) =>
          value > accumulator ? value : accumulator
        ) +
      "ms"
  );
  console.log("Rate: " + Math.ceil(requestsPerSecond) + " requests/s");
  console.log(
    "Total requests processed: " +
      requests +
      " (" +
      positives +
      " blacklisted, " +
      negatives +
      " not blacklisted)"
  );
}

function checkResults(
  results,
  requestsWithUnexpectedStatus,
  requestsOverMaxThreshold,
  averageLatency,
  requestsPerSecond
) {
  if (results.length !== requests) {
    throw new Error(requests - results.length + "requests did not finish.");
  }
  if (requestsWithUnexpectedStatus.length > 0) {
    throw new Error(
      requestsWithUnexpectedStatus.length +
        " requests with unexpected status: " +
        JSON.stringify(requestsWithUnexpectedStatus)
    );
  }
  if (requestsOverMaxThreshold.length > 0) {
    throw new Error(
      requestsOverMaxThreshold.length + " requests over max threshold."
    );
  }
  if (averageLatency > 50) {
    throw new Error("Average latency over threshold: " + averageLatency);
  }
  if (requestsPerSecond < 900) {
    throw new Error(
      "Could not match desired rate of at least 900 requests/s: " +
        requestsPerSecond
    );
  }
}
