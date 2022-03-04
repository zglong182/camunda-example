package cn.zglong.example.camunda.controller

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method


def http = new HTTPBuilder('http://localhost:11001')
String manager = '';
http.request(Method.POST, ContentType.JSON) {
    req ->
        //请求URL路径信息
        uri.path = '/operate/add'
        def user = ['start':2,'end':1]
        def params = ['id': 1, "name": user]
        // 请求参数信息 [id:'666',name:'jake']
//        uri.query = params
        // 请求体信息
        body = params
        // 设置请求头信息
        headers.'User-Agent' = 'Mozill/5.0'
        headers.'local' = 'zh_CN'
        response.success = {
            resp, reader ->
                assert resp.statusLine.statusCode == 200
                println reader.name
                manager = reader.name
        }
        response.'404' = {
            println 'Not found'
        }
}
println manager
task.setVariable("manager", manager)


