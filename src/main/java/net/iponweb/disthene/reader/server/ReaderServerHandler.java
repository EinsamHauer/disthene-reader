package net.iponweb.disthene.reader.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.exceptions.EvaluationException;
import net.iponweb.disthene.reader.exceptions.LogarithmicScaleNotAllowed;
import net.iponweb.disthene.reader.exceptions.ParameterParsingException;
import net.iponweb.disthene.reader.handler.DistheneReaderHandler;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Andrei Ivanov
 */
public class ReaderServerHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = Logger.getLogger(ReaderServerHandler.class);

    private final Map<Pattern, DistheneReaderHandler> handlers;

    ReaderServerHandler(Map<Pattern, DistheneReaderHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {

        try {
            HttpRequest request = (HttpRequest) message;
            boolean keepAlive = HttpUtil.isKeepAlive(request);

            FullHttpResponse response;

            if (HttpUtil.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }

            logger.debug("Got request: " + ((HttpRequest) message).method() + " " + ((HttpRequest) message).uri());
            byte[] bytes = new byte[((HttpContent) message).content().readableBytes()];
            ((HttpContent) message).content().readBytes(bytes);
            logger.debug("Request content: " + new String(bytes));

            String path = new QueryStringDecoder(((HttpRequest) message).uri()).path();

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
            }

            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response, ctx.voidPromise());
            } else {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (EvaluationException | ParameterParsingException | LogarithmicScaleNotAllowed e) {
            FullHttpResponse response;
            if (e.getCause() != null) {
                response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, Unpooled.wrappedBuffer(("Ohoho.. We have a problem: " + e.getCause().getMessage()).getBytes()));
            } else {
                response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, Unpooled.wrappedBuffer(("Ohoho.. We have a problem: " + e.getMessage()).getBytes()));
            }
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            logger.error("Invalid request: " + e.getMessage());
            logger.debug("Invalid request: ", e);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(("Ohoho.. We have a problem: " + e.getMessage()).getBytes()));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } finally {
            ((HttpContent) message).content().release();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception while processing request", cause);
        ctx.close();
    }
}
