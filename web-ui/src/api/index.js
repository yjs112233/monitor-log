import request from '@/utils/request'

// 接口列表
export function listPost(params) {
    return request({
        url: '/mxkj/log',
        method: 'get',
        params
    })
}
// 登录
export function login(data) {
    return request({
        url: `/mxkj/login?username=${data.username}&password=${data.password}`,
        method: 'post'
    })
}