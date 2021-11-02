const handlerProxy = require("nbb-cloud-functions");

module.exports = {
  handler: handlerProxy("example.cljs", "handler"),
};
