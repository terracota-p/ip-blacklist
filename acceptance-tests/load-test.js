const { ipBlacklistRequests, checkResults } = require("./ipBlacklistRequests");

ipBlacklistRequests(2000, (err, results) => checkResults(results));
