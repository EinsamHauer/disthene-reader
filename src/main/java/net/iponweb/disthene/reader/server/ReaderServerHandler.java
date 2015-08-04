package net.iponweb.disthene.reader.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.handler.DistheneReaderHandler;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Andrei Ivanov
 */
public class ReaderServerHandler extends ChannelInboundHandlerAdapter {
    final static Logger logger = Logger.getLogger(ReaderServerHandler.class);

    private Map<Pattern, DistheneReaderHandler> handlers = new HashMap<>();

    public ReaderServerHandler(Map<Pattern, DistheneReaderHandler> handlers) {
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

            logger.debug("Got request: " + ((HttpRequest) message).getMethod() + " " + ((HttpRequest) message).getUri());
            byte[] bytes = new byte[((HttpContent) message).content().readableBytes()];
            ((HttpContent) message).content().readBytes(bytes);
            logger.debug("Request content: " + new String(bytes));

            String path = new QueryStringDecoder(((HttpRequest) message).getUri()).path();

            DistheneReaderHandler handler = null;
            for(Map.Entry<Pattern,DistheneReaderHandler> entry : handlers.entrySet()) {
                if (entry.getKey().matcher(path).matches()) {
                    handler = entry.getValue();
                    break;
                }
            }


            if (handler != null) {
                response = handler.handle(request);
            } else {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }

            if (keepAlive) {
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                ctx.write(response);
            } else {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            logger.error("Invalid request: " + e.getMessage());
            logger.debug("Invalid request: ", e);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(("Ohoho.. We have a problem: " + e.getMessage()).getBytes()));
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
