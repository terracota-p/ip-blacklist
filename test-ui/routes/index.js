const express = require("express");
const router = express.Router();
const { getIndex } = require("../controller/indexController");

/* GET home page. */
router.get("/", getIndex);

module.exports = router;
