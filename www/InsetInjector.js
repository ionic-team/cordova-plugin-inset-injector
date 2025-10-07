var exec = require("cordova/exec");

window.setTimeout(function () {
  exec((_) => _, null, "InsetInjector", "_ready", []);
}, 0);
