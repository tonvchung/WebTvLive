(function (root) {
  'use strict';

  var ENHANCEMENT_LEVELS = { original: 1, light: 1, standard: 1, strong: 1 };

  /** CSS color-function profile for a video enhancement level, or null for "original". */
  function enhancementProfile(level) {
    switch (level) {
      case 'light': return { contrast: 1.03, saturation: 1.02, brightness: 1.00 };
      case 'standard': return { contrast: 1.06, saturation: 1.03, brightness: 1.01 };
      case 'strong': return { contrast: 1.10, saturation: 1.05, brightness: 1.02 };
      default: return null;
    }
  }

  /** Clamp an arbitrary value to a known enhancement level, defaulting to "original". */
  function normalizeEnhancementLevel(level) {
    var value = String(level);
    return Object.prototype.hasOwnProperty.call(ENHANCEMENT_LEVELS, value) ? value : 'original';
  }

  /**
   * Score a candidate <video> when picking the active player. During a channel switch, a node
   * different from the previous player is strongly preferred so playback tracks the new stream.
   */
  function videoScore(opts) {
    var replacementBonus = opts.isReplacementCandidate ? 10000000 : 0;
    var area = opts.width * opts.height;
    var readyBonus = opts.readyState >= 2 ? 2000000 : 0;
    var playingBonus = opts.paused ? 0 : 1000000;
    return replacementBonus + area + readyBonus + playingBonus;
  }

  /** Score a candidate <iframe> when no <video> is available; player-ish sources win. */
  function playerFrameScore(width, height, src) {
    var sourceBonus = /(player|live|cntv|cctv|yangshipin)/.test(String(src).toLowerCase())
      ? 10000000
      : 0;
    return width * height + sourceBonus;
  }

  /** Return a comparable vertical resolution for an official player quality option. */
  function qualityValue(raw) {
    if (typeof raw === 'number') return isFinite(raw) && raw > 0 ? raw : 0;
    var text = String(raw === undefined || raw === null ? '' : raw).trim().toLowerCase();
    if (!text || text === 'auto' || text === '自动') return 0;
    if (/8\s*k/.test(text)) return 4320;
    if (/4\s*k/.test(text)) return 2160;

    var numeric = text.match(/(\d{3,4})\s*p?/);
    if (numeric) {
      var value = Number(numeric[1]);
      return value >= 180 && value <= 4320 ? value : 0;
    }
    if (text.indexOf('蓝光') >= 0) return 1080;
    if (text.indexOf('超清') >= 0) return 720;
    if (text.indexOf('高清') >= 0) return 540;
    if (text.indexOf('标清') >= 0) return 480;
    if (text.indexOf('流畅') >= 0) return 360;
    return 0;
  }

  /** Pick the highest publicly listed quality; automatic/unknown options never win. */
  function highestQualityIndex(values) {
    var bestIndex = -1;
    var bestValue = 0;
    for (var i = 0; i < values.length; i++) {
      var value = qualityValue(values[i]);
      if (value > bestValue) {
        bestValue = value;
        bestIndex = i;
      }
    }
    return bestIndex;
  }

  /** A guard-requested pause must never be undone by the site's autoplay policy. */
  function shouldAttemptPlayback(playbackAllowed, paused, waitingForReplacement, replayComplete) {
    return !!(playbackAllowed && paused && !waitingForReplacement && replayComplete);
  }

  root.WebTvLivePlayerAdapterCore = {
    enhancementProfile: enhancementProfile,
    shouldAttemptPlayback: shouldAttemptPlayback,
    normalizeEnhancementLevel: normalizeEnhancementLevel,
    videoScore: videoScore,
    playerFrameScore: playerFrameScore,
    qualityValue: qualityValue,
    highestQualityIndex: highestQualityIndex,
  };
})(typeof window !== 'undefined' ? window : this);
