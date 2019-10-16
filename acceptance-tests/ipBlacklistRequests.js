const { randomIp } = require("./randomIp");
const async = require("async");
const rp = require("request-promise");

const AVG_LATENCY_THRESHOLD = 50;
const MAX_LATENCY_THRESHOLD = 200;

exports.ipBlacklistRequests = function ipBlacklistRequests(requests, callback) {
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
    collectResults(overallStartTime, requests, callback)
  );
};

function collectResults(overallStartTime, requests, callback) {
  return (err, results) => {
    if (err) throw err;
    // results is now an array of the responses

    const overallTime = new Date().getTime() - overallStartTime;
    const requestsPerSecond = (requests * 1000) / overallTime;
    const aggregatedResults = aggregateResults(
      results,
      requests,
      requestsPerSecond
    );
    printResultsSummary(aggregatedResults);

    callback && callback(null, aggregatedResults);
  };
}

function aggregateResults(results, requests, requestsPerSecond) {
  const requestsWithUnexpectedStatus = results.filter(
    response => response.statusCode !== 200 && response.statusCode !== 204
  );
  const requestsOverMaxLatency = results.filter(
    response => response.elapsedTime > MAX_LATENCY_THRESHOLD
  );
  const averageLatency =
    results
      .map(response => response.elapsedTime)
      .reduce((accumulator, value) => accumulator + value) / requests;
  const maxLatency = results
    .map(response => response.elapsedTime)
    .reduce((accumulator, value) =>
      value > accumulator ? value : accumulator
    );
  const positives = results.filter(response => response.statusCode === 200)
    .length;
  const negatives = results.filter(response => response.statusCode === 204)
    .length;
  return {
    averageLatency,
    maxLatency,
    requestsWithUnexpectedStatus,
    requestsOverMaxLatency,
    positives,
    negatives,
    requestsPerSecond,
    requests,
    results
  };
}

function printResultsSummary(aggregatedResults) {
  const {
    averageLatency,
    maxLatency,
    requestsWithUnexpectedStatus,
    requestsOverMaxLatency,
    positives,
    negatives,
    requestsPerSecond,
    requests,
    results
  } = aggregatedResults;
  console.log("average latency: " + averageLatency + "ms");
  console.log("max latency: " + maxLatency + "ms");
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

function checkResults(aggregatedResults) {
  const {
    averageLatency,
    requestsWithUnexpectedStatus,
    requestsOverMaxLatency,
    positives,
    negatives,
    requestsPerSecond,
    requests,
    results
  } = aggregatedResults;

  if (results.length !== requests) {
    throw new Error(requests - results.length + " requests did not finish.");
  }
  if (requestsWithUnexpectedStatus.length > 0) {
    throw new Error(
      requestsWithUnexpectedStatus.length +
        " requests with unexpected status: " +
        JSON.stringify(requestsWithUnexpectedStatus)
    );
  }
  if (requestsOverMaxLatency.length > 0) {
    throw new Error(
      requestsOverMaxLatency.length +
        " requests over max threshold (" +
        MAX_LATENCY_THRESHOLD +
        "ms)."
    );
  }
  if (averageLatency > AVG_LATENCY_THRESHOLD) {
    throw new Error(
      "Average latency over threshold: " +
        averageLatency +
        " (threshold is " +
        AVG_LATENCY_THRESHOLD +
        ")"
    );
  }
  // Desired throughput would be ~770, but that was too restrictive for consistent green result right after startup.
  // 330 consistently works, and is reasonable given that the main purpose of this test is measure latency, and in prod we'd
  // probably want 3 service instances (for high availability), that would yield ~1000 req/s min throughput right after startup.
  if (requestsPerSecond < 330) {
    throw new Error(
      "Could not match desired rate of at least 330 requests/s: " +
        requestsPerSecond
    );
  }
}
exports.checkResults = checkResults;
