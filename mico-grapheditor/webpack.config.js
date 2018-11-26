const path = require('path');

module.exports = {
    entry: "./src/index.ts",

    // Enable sourcemaps for debugging webpack's output.
    devtool: "source-map",

    resolve: {
        // Add '.ts' and '.tsx' as resolvable extensions.
        extensions: [".webpack.js", ".web.js", ".ts", ".js"]
    },

    module: {
        rules: [
            {
                test: /\.[jt]s$/,
                use: "awesome-typescript-loader",
                exclude: path.resolve(__dirname, "node_modules")
            }
        ]
    },
    output: {
        filename: "bundle.js",
        path: path.resolve(__dirname, 'dist')
    },
};
