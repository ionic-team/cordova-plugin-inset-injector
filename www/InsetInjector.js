var exec = require("cordova/exec");

// Only initialize the plugin on Android where it's supported
window.setTimeout(function () {
  if (cordova.platformId === 'android') {
    exec((_) => _, null, "InsetInjector", "_ready", []);
  }
}, 0);
