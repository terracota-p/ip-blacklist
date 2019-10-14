const fetch = require("node-fetch");
const backendUrl = require("../util/backendUrl");

async function queryBlacklist(ip) {
  return await get(backendUrl() + "/ips/" + ip);
}

module.exports = queryBlacklist;

async function get(url) {
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(res.statusText);
  }
  if (res.status === 204) {
    return;
  } else {
    return await res.json();
  }
}
