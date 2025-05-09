const webpack = require('webpack');

module.exports = function override(config) {
  config.resolve.fallback = {
    "http": require.resolve("stream-http"),
    "https": require.resolve("https-browserify"),
    "stream": require.resolve("stream-browserify"),
    "util": require.resolve("util/"),
    "crypto": require.resolve("crypto-browserify"),
    "url": require.resolve("url/"),
    "assert": require.resolve("assert/"),
    "zlib": require.resolve("browserify-zlib")
  };
  return config;
};