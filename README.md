# cordova-plugin-inset-injector

On several versions of the Android WebView there are issues with the built in `env(safe-area-inset-*)` values where they are reported incorrectly. This plugin remedies this issue by injecting the correct values into css variables named `--safe-area-inset-*`, where * can be top, bottom, left, and right.

**⚠️ Platform Support: This plugin only works on Android and is designed to gracefully handle other platforms without breaking builds.**

## Installation

```bash
npx cordova plugin add cordova-plugin-inset-injector
```

## Supported Platforms

- **Android**: Full functionality
- **iOS**: Plugin is available but does nothing (safe fallback)
- **Other platforms**: Plugin is available but does nothing (safe fallback)

## Usage

No additional setup required! The plugin automatically initializes on Android devices and starts injecting CSS variables. On other platforms, it safely does nothing.

Simply install the plugin and use the CSS variables in your styles:

```css
.my-content {
  padding-top: var(--safe-area-inset-top, 0px);
  padding-bottom: var(--safe-area-inset-bottom, 0px);
  padding-left: var(--safe-area-inset-left, 0px);
  padding-right: var(--safe-area-inset-right, 0px);
}
```

## Platform-Specific Behavior

### Android
- Plugin actively monitors system UI changes
- Injects CSS variables: `--safe-area-inset-top`, `--safe-area-inset-bottom`, `--safe-area-inset-left`, `--safe-area-inset-right`
- Handles edge-to-edge UI scenarios

### iOS and Other Platforms
- Plugin loads without errors
- All methods return safely without attempting native calls
- Does not interfere with existing iOS safe area handling
- Allows cross-platform apps to include this plugin without platform-specific conditional installation