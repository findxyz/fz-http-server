package xyz.fz.http.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        String httpRequestInfo = "------------------------- full request header -------------------------"
                + "\r\n" + msg + "\r\n"
                + "------------------------- full request body -------------------------"
                + "\r\n" + msg.content().toString(CharsetUtil.UTF_8) + "\r\n"
                + "------------------------- response html -------------------------"
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
