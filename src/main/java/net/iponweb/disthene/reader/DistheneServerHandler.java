package net.iponweb.disthene.reader;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Andrei Ivanov
 */
public class DistheneServerHandler extends ChannelInboundHandlerAdapter {
    final static Logger logger = Logger.getLogger(DistheneServerHandler.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (DefaultHttpHeaders.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            boolean keepAlive = DefaultHttpHeaders.isKeepAlive(req);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(getResponse(req).getBytes()));
            response.headers().set(CONTENT_TYPE, "application/json");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private String getResponse(HttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        String path = decoder.path();

        logger.debug("Query was: " + request.getUri());
        logger.debug("Method was: " + request.getMethod());

        switch (path) {
            case "/metrics":
                try {
                    return MetricsResponseBuilder.buildResponse(
                            decoder.parameters().get("tenant").get(0),
                            decoder.parameters().get("path").get(0),
                            Long.valueOf(decoder.parameters().get("from").get(0)),
                            Long.valueOf(decoder.parameters().get("to").get(0)));
                } catch (Exception e) {
                    logger.error(e);
                    return "Error";
                }
            case "/paths":
                try {
                    return PathsResponseBuilder.buildResponse(
                            decoder.parameters().get("tenant") == null ? "NONE" : decoder.parameters().get("tenant").get(0),
                            decoder.parameters().get("query") == null ? "*" : decoder.parameters().get("query").get(0)
                            );
                } catch (Exception e) {
                    logger.error("Encountered an error fetching paths", e);
                    logger.error("Parameters were:" + decoder.parameters());

                    return "Error";
                }
            default:
                return "Error";
        }

    }
}
