const path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const HTMLInlineCSSWebpackPlugin = require("html-inline-css-webpack-plugin").default;

module.exports = {
    mode: 'production',
    entry: './src/index.js',
    optimization: {
        minimizer: [
            new CssMinimizerPlugin({
                minify: CssMinimizerPlugin.cleanCssMinify,
                minimizerOptions: {
                    format: 'beautify',
                    preset: [
                        "default",
                        {
                            discardComments: {
                                removeAll: true
                            },
                        },
                    ],
                },
            }),
        ],
    },
    devServer: {
        static: path.join(__dirname, 'dist'),
        watchFiles: ["./src/**/*"],
        hot: true
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'app.bundle.js',
        clean: true,
    },
    plugins: [
        new MiniCssExtractPlugin(),
        ...['Invoice', 'CreditNote'].map((documentType) => new HtmlWebpackPlugin({
            title: 'Project OpenUBL',
            filename: `${documentType}.html`,
            template: `src/${documentType}.ejs`,
            minify: false
        })),
        new HTMLInlineCSSWebpackPlugin()
    ],
    module: {
        rules: [
            {
                test: /\.css$/i,
                include: path.resolve(__dirname, 'src'),
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    'postcss-loader',
                ],
            },
        ],
    },
};