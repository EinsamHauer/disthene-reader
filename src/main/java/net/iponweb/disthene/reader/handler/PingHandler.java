package net.iponweb.disthene.reader.handler;

import io.netty.handler.codec.http.*;

/**
 * @author Andrei Ivanov
 */
public class PingHandler implements DistheneReaderHandler {

    @Override
    public FullHttpResponse handle(HttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return response;
    }
}
