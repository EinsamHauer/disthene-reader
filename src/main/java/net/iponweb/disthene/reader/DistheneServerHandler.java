package net.iponweb.disthene.reader;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
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

        try {
            Map<String, String> parameters = decodeParameters(msg);
            HttpRequest req = (HttpRequest) msg;
            QueryStringDecoder decoder = new QueryStringDecoder(req.getUri());
            String path = decoder.path();

            logger.debug("Path: " + path);
            for(Map.Entry<String, String> param : parameters.entrySet()) {
                logger.debug(param.getKey() + "=" + param.getValue());
            }

            if (DefaultHttpHeaders.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            boolean keepAlive = DefaultHttpHeaders.isKeepAlive(req);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(getResponse(path, parameters).getBytes()));
            response.headers().set(CONTENT_TYPE, "application/json");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                ctx.write(response);
            }
        } catch (Exception e) {
            logger.error("Invalid request", e);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            ctx.write(response);
        }
    }

    private Map<String, String> decodeParameters(Object msg) throws IOException {
        Map<String, String> result = new HashMap<>();
        if (msg instanceof HttpRequest) {

            if (((HttpRequest) msg).getMethod().equals(HttpMethod.POST)) {
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder((HttpRequest) msg);
                for(InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                    if (data instanceof Attribute) {
                        result.put(data.getName(), ((Attribute) data).getValue());
                    }
                }
            } else {
                QueryStringDecoder decoder = new QueryStringDecoder(((HttpRequest) msg).getUri());
                for(Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                    result.put(entry.getKey(), entry.getValue().size() > 0 ? entry.getValue().get(0) : null);
                }


            }

        }
        logger.debug(result);

        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private String getResponse(String path, Map<String, String> parameters) throws Exception {
        switch (path) {
            case "/metrics":
                    return MetricsResponseBuilder.buildResponse(
                            parameters.get("tenant"),
                            parameters.get("path"),
                            Long.valueOf(parameters.get("from")),
                            Long.valueOf(parameters.get("to")));
            case "/paths":
                    return PathsResponseBuilder.buildResponse(
                            parameters.get("tenant"),
                            parameters.get("query") == null ? "*" : parameters.get("query")
                            );
            default:
                throw new UnsupportedOperationException("Path not supported");
        }

    }
}
