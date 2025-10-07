# cordova-plugin-inset-injector

On several versions of the Android WebView there are issues with the built in `env(safe-area-inset-*)` values where they are reported incorrectly. This plugin remedies this issue by injecting the correct values into css variables named `--safe-area-inset-*`, where * can be top, bottom, left, and right

## Installation

```
npx cordova plugin add cordova-plugin-inset-injector.
```

## Supported Platforms

- Android