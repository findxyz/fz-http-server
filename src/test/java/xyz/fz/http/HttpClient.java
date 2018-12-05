package xyz.fz.http;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class HttpClient {

    private void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 59259))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpContentDecompressor());
                            ch.pipeline().addLast(new HttpObjectAggregator(64 * 1024));
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    private static class ClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
                    "/hello", Unpooled.copiedBuffer("name=fz", CharsetUtil.UTF_8));
            request.headers()
                    .add("Content-Type", "application/x-www-form-urlencoded")
                    .add("Accept-Encoding", "gzip, deflate, br")
                    .add("Content-Length", request.content().readableBytes());
            ctx.writeAndFlush(request);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
            System.out.println("============================ full http response header ============================");
            System.out.println(msg);
            System.out.println("============================ full http response body ============================");
            System.out.println(msg.content().toString(CharsetUtil.UTF_8).replace("<br/>", "\r\n"));
            ctx.channel().close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        new HttpClient().start();
    }
}
