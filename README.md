# NettyWebFrame

##这是一个轻量级的基于Netty的Web框架。仅仅是一个雏形，喜欢有兴趣的大佬可以一起来完善。

##目前该框架已经支持HTTP协议的处理和WEBSOCKET协议的处理

##HTTP部分目前只做了常用的GET/POST请求的支持，后续可扩展

##使用该框架可以轻松的开发web应用接口，提供了一些类似于Spring框架的注解,例如@Controller, @RequestMapping,@RequestBody,@Param，@RestController，@PathParam.

##框架使用教程

```java
//普通http接口
@Controller
public class TestController {

    @RequestMapping(url = "/xxx",method = RequestMapping.Method.POST)
    public Object test(FullHttpRequest fullHttpRequest, @Param("xxx")String a, @RequestBody Pojo pojo, @MapperParam TestMapper testMapper){
        System.out.println(a);
        System.out.println(JSON.toJSONString(pojo));
        Container container = HttpServerLauncher.getContainer();
        container.getClass("");
        Integer integer = testMapper.select1();
        System.out.println(integer);
        return null;
    }
}
//rest http接口
@RestController
public class TestRestController {

    @RestRequestMapping(url = "/{appId}/aaa",method = RestRequestMapping.Method.POST)
    public Object test(@PathParam("appId") String appId, @Param("bbb")String bbb, @RequestBody Pojo pojo, FullHttpRequest fullHttpRequest, FullHttpResponse fullHttpResponse){
        System.out.println(appId);
        System.out.println(bbb);
        System.out.println(JSON.toJSONString(pojo));
        JSONObject res=new JSONObject();
        res.put("aaa","xxx");
        return res;
    }

    @RestRequestMapping(url = "/{appId}/aaa1",method = RestRequestMapping.Method.POST)
    public Object test1(@PathParam("appId") String appId, @Param("bbb")String bbb, @RequestBody Pojo pojo, FullHttpRequest fullHttpRequest, FullHttpResponse fullHttpResponse){
        System.out.println(appId);
        System.out.println(bbb);
        System.out.println(JSON.toJSONString(pojo));
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        fullHttpResponse.content().writeBytes("xxx".getBytes());
        fullHttpResponse.content().writeBytes(Unpooled.wrappedBuffer("xxxxx".getBytes()));
        return fullHttpResponse;
    }
}
```



