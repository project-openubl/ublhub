module.exports = {
  createOldCatalogs: true, // Save the \_old files

  indentation: 4, // Indentation of the catalog files

  keepRemoved: false, // Keep keys from the catalog that are no longer in code

  lexers: {
    js: ["JsxLexer"],
    ts: ["JsxLexer"],
    jsx: ["JsxLexer"],
    tsx: ["JsxLexer"],

    default: ["JsxLexer"],
  },

  locales: ["es"],

  output: "public/locales/$LOCALE/$NAMESPACE.json",

  input: ["src/**/*.{js,jsx,ts,tsx}"],

  sort: true,
  verbose: true,
};
