// tweaks-panel.jsx - Controls/tweaks panel integration script
const tweaksHelper = {
  logState: (component, value) => {
    console.log(`[Tweak] Component ${component} modified to:`, value);
  }
};
window.tweaksHelper = tweaksHelper;
