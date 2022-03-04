package cn.zglong.example.camunda.controller

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method


def http = new HTTPBuilder('http://yapi.zglong.cn')
String manager = '';
http.request(Method.GET, ContentType.JSON) {
    req ->
        //请求URL路径信息
        uri.path = '/mock/20/temp/get-superior'
        //请求参数信息 [id:'666',name:'jake']
//        def params = ['id': 1, "name": 'zglong']
//        uri.query = params
        response.success = {
            resp, reader ->
                assert resp.statusLine.statusCode == 200
                println reader.user
                manager = reader.user
        }
        response.'404' = {
            println 'Not found'
        }
}
println manager
task.setVariable("manager", manager)

