const { When, Then } = require("cucumber");

const {
  ipBlacklistRequests,
  checkResults
} = require("../../ipBlacklistRequests");

var ipBlacklistResults;
When(/^I send (.*) requests to get IP$/, function(requests, callback) {
  ipBlacklistRequests(Number(requests), (err, results) => {
    ipBlacklistResults = results;
    callback();
  });
});

Then(
  /^maximum latency is below 200 ms and average latency is below 50 ms$/,
  function(callback) {
    checkResults(ipBlacklistResults);
    callback();
  }
);
