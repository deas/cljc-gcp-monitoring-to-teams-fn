const handlerProxy = require("nbb-cloud-functions");

module.exports = {
  handle: handlerProxy("example.cljs", "handler"),
};
