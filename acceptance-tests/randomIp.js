const randomip = require("random-ip");

function randomIp() {
  return randomip("0.0.0.0", 0);
}

exports.randomIp = randomIp;
