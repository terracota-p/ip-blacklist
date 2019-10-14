function backendUrl() {
  const devUrl = "http://localhost:8080";
  return process.env.BACKEND_URL || devUrl;
}

module.exports = backendUrl;
