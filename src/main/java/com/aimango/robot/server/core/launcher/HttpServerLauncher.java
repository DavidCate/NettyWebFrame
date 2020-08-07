package com.aimango.robot.server.core.launcher;


import com.aimango.robot.server.core.component.Nacos;
import com.aimango.robot.server.core.component.PropertiesUtils;
import com.aimango.robot.server.core.constant.ServerConfig;
import com.aimango.robot.server.core.container.Container;
import com.aimango.robot.server.core.container.ContainerBuilder;
import com.aimango.robot.server.core.initializer.RobotServerInitializer;
import com.aimango.robot.server.utils.NacosClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class HttpServerLauncher {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerLauncher.class);

    private EventLoopGroup bossGroup = new NioEventLoopGroup();

    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ServerBootstrap serverBootstrap = new ServerBootstrap();

    int port=8080;

    private static Container container;

    private String basePackage;

    private String configMode= ServerConfig.Config.CONFIG_MODE_LOCAL;

    public static void run(Class clazz, String[] args) {
        String className = clazz.getName();
        int index = className.lastIndexOf('.');
        String packageName=className.substring(0,index);
        HttpServerLauncher httpServerLauncher=new HttpServerLauncher();
        httpServerLauncher.basePackage=packageName;
        httpServerLauncher.launch();
    }

    private HttpServerLauncher init() throws Exception {
        logger.info("服务器初始化");
        serverInit();
        containerInit();
        return this;
    }

    private void containerInit() throws Exception {
        logger.info("初始化容器");
        if (container!=null){
            throw new Exception("容器已经存在！");
        }
        ContainerBuilder containerBuilder=new ContainerBuilder(basePackage);
        Container container = containerBuilder.build();
        HttpServerLauncher.container=container;
    }

    private void serverInit(){
        logger.info("服务器配置初始化");
        //配置工作线程组
        serverBootstrap.group(bossGroup, workerGroup);
        //配置通道类型
        serverBootstrap.channel(NioServerSocketChannel.class);
        //设置请求等待队列
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        //开启TCP_NODELAY
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        //开启SO_KEEPALIVE
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        //配置receiveBuf使用混合型
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator());
        //配置初始化处理器队列
        serverBootstrap.childHandler(new RobotServerInitializer());
    }

    public void launch(){
        logger.info("服务器启动程序");
        HttpServerLauncher launcher = null;
        try {
            launcher=init();
            start(launcher);
        }catch (Exception e) {
            Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownBefore(launcher,e)));
        } finally {
            if (launcher!=null){
                launcher.workerGroup.shutdownGracefully();
                launcher.bossGroup.shutdownGracefully();
            }
        }
    }

    private void start(HttpServerLauncher launcher) throws InterruptedException, IOException {
        if (configMode.equals(ServerConfig.Config.CONFIG_MODE_NACOS)){
            this.port =Integer.parseInt(Nacos.getPropertiesField(ServerConfig.SERVER_PORT)) ;
        }else {
            this.port =Integer.parseInt(PropertiesUtils.getProperty("server.port")) ;
        }
        ChannelFuture channelFuture = launcher.serverBootstrap.bind("0.0.0.0",this.port).sync();
        logger.info("服务已启动，监听端口:"+ this.port);
        serverRegister();
        channelFuture.channel().closeFuture().sync();
    }

    private void serverRegister(){
        NacosClient.regist();
    }

    public static HttpServerLauncher newInstance(){
        return new HttpServerLauncher();
    }

    class ShutDownBefore implements Runnable {
        private HttpServerLauncher httpServerLauncher;
        private Exception exception;
        public ShutDownBefore(HttpServerLauncher httpServerLauncher, Exception exception) {
            this.httpServerLauncher=httpServerLauncher;
            this.exception=exception;
        }

        @Override
        public void run() {
            logger.error("服务异常关闭",exception);
        }
    }

    public static Container getContainer() {
        if (container==null){
            throw new NullPointerException("容器未初始化");
        }
        return container;
    }
}
