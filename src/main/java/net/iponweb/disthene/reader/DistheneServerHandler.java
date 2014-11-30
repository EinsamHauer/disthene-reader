package net.iponweb.disthene.reader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import net.iponweb.disthene.reader.response.RequestDispatcher;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
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
    public void channelRead(ChannelHandlerContext ctx, Object message) {

        try {
            HttpRequest request = (HttpRequest) message;

            if (DefaultHttpHeaders.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            FullHttpResponse response = new RequestDispatcher(message).getResponse();
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            logger.error("Invalid request", e);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            ctx.write(response);
        }
    }

    private Map<String, String> decodeParameters(Object msg, String path) throws IOException {
        Map<String, String> result = new HashMap<>();

        switch (path) {
            case "/metrics":
                QueryStringDecoder decoder = new QueryStringDecoder(((HttpRequest) msg).getUri());
                for(Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                    result.put(entry.getKey(), entry.getValue().size() > 0 ? entry.getValue().get(0) : null);
                }
                break;
            case "/paths":
                logger.debug("POST request");
                logger.debug(((HttpContent) msg).content().toString(Charset.defaultCharset()));
                Gson gson = new Gson();
                Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
                Map<String,String> map = gson.fromJson(((HttpContent) msg).content().toString(Charset.defaultCharset()), stringStringMap);

                for(Map.Entry<String,String> param : map.entrySet()) {
                    result.put(param.getKey(), param.getValue());
                }
                break;
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
                throw new UnsupportedOperationException("Path not supported: " + path);
        }

    }

}
