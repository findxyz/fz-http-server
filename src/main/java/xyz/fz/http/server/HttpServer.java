package xyz.fz.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    @Value("${http.server.port}")
    private int httpServerPort;

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(64 * 1024));
                            ch.pipeline().addLast(new HttpContentCompressor());
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = serverBootstrap.bind("0.0.0.0", httpServerPort).sync();

            LOGGER.warn("http server startup...");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            LOGGER.warn("http server shutdown...");
        }
    }

    private static class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

            String httpRequestInfo = "---------------------------- full request header ----------------------------"
                    + "\r\n" + msg + "\r\n"
                    + "---------------------------- full request body ----------------------------"
                    + "\r\n" + msg.content().toString(CharsetUtil.UTF_8) + "\r\n"
                    + "---------------------------- response html ----------------------------"
                    + "\r\n" + "<html><head>aaa</head><body>bbb</body></html>" + "\r\n";

            // full http request
            System.out.println(httpRequestInfo);

            // write full http response
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(httpRequestInfo.replace("\r\n", "<br/>"), CharsetUtil.UTF_8));

            response.headers()
                    .add(HttpHeaderNames.CONTENT_TYPE, DEFAULT_CONTENT_TYPE)
                    .add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())
                    .add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            ctx.writeAndFlush(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
