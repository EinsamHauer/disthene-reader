package net.iponweb.disthene.reader.handler;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import net.iponweb.disthene.reader.exceptions.*;

import java.util.concurrent.ExecutionException;

/**
 * @author Andrei Ivanov
 */
public interface DistheneReaderHandler {

    FullHttpResponse handle(HttpRequest request) throws ParameterParsingException, ExecutionException, InterruptedException, EvaluationException, LogarithmicScaleNotAllowed, TooMuchDataExpectedException;
}
