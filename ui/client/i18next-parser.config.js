/* eslint-env node */

module.exports = {
  createOldCatalogs: true, // Save the \_old files

  indentation: 2, // Indentation of the catalog files

  keepRemoved: true, // Keep keys from the catalog that are no longer in code

  lexers: {
    js: ["JsxLexer"],
    ts: ["JsxLexer"],
    jsx: ["JsxLexer"],
    tsx: ["JsxLexer"],

    default: ["JsxLexer"],
  },

  locales: ["en", "es"],

  output: "public/locales/$LOCALE/$NAMESPACE.json",

  input: ["src/**/*.{js,jsx,ts,tsx}"],

  sort: true,
  verbose: true,
};
