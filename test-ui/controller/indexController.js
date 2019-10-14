const queryBlacklist = require("../domain/queryBlacklist");

exports.getIndex = async function getIndex(req, res) {
  const ip = req.query.ip;
  if (!ip) {
    renderEmpty(res);
  } else {
    await queryAndRenderResults(ip, res);
  }
};

async function queryAndRenderResults(ip, res) {
  try {
    const blacklistedResult = await queryBlacklist(ip);
    renderWithResults(res, ip, blacklistedResult);
  } catch (err) {
    renderError(res, ip, err);
  }
}

function renderEmpty(res) {
  res.render("index", {});
}

function renderWithResults(res, ip, blacklistedResult) {
  res.render("index", {
    ip: ip,
    blacklistedResult: blacklistedResult,
    goodResult: !blacklistedResult
  });
}

function renderError(res, ip, err) {
  res.render("index", {
    ip: ip,
    error: err.message
  });
}
