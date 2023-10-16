const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    publicPath: process.env.VUE_APP_BASE_API + '/mxkj',
    devServer: {
        proxy: {
            // detail: https://cli.vuejs.org/config/#devserver-proxy
            '/': {
                target: `http://192.168.10.80:20001`, //本地
                changeOrigin: true,
                // pathRewrite: {
                //   ['^/api']: ''
                // }
            }
        },
    },
})