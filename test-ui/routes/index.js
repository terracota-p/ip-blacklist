var express = require("express");
var router = express.Router();

/* GET home page. */
router.get("/", async function(req, res) {
  if (!req.query.ip) {
    res.render("index", { title: "anomaly-detections-ip" });
  } else {
    const ip = req.query.ip;
    const result = await fetchIp(ip);
    res.render("index", {
      title: "anomaly-detections-ip - " + ip,
      ip: ip,
      result: result
    });
  }
});

async function fetchIp(ip) {
  // TODO
  return await get(backendUrl() + "/ips/" + ip);
}

async function get(url) {
  const req = new Request(url, {
    method: "GET"
  });
  const res = await fetch(req);
  if (!res.ok) {
    throw new Error(await errorMessage(res));
  }
  return await res.json();
}

function backendUrl() {
  const devUrl = "http://localhost:8080";
  return process.env.BACKEND_URL || devUrl;
}
module.exports = router;
