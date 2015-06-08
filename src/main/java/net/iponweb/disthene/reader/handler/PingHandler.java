package net.iponweb.disthene.reader.handler;

import io.netty.handler.codec.http.*;

/**
 * @author Andrei Ivanov
 */
public class PingHandler implements DistheneReaderHandler {

    @Override
    public FullHttpResponse handle(HttpRequest request) {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }
}
