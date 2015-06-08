package net.iponweb.disthene.reader.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.handler.DistheneReaderHandler;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Andrei Ivanov
 */
public class ReaderServerHandler extends ChannelInboundHandlerAdapter {
    final static Logger logger = Logger.getLogger(ReaderServerHandler.class);

    private Map<String, DistheneReaderHandler> handlers = new HashMap<>();

    public ReaderServerHandler(Map<String, DistheneReaderHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {

        try {
            HttpRequest request = (HttpRequest) message;
            boolean keepAlive = HttpHeaders.isKeepAlive(request);

            FullHttpResponse response;

            if (DefaultHttpHeaders.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }

            String path = new QueryStringDecoder(((HttpRequest) message).getUri()).path();
            DistheneReaderHandler handler = handlers.get(path);

            if (handler != null) {
                response = handler.handle(request);
            } else {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            }

            if (keepAlive) {
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                ctx.write(response);
            } else {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            logger.error("Invalid request", e);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception while processing request", cause);
        ctx.close();
    }
}
