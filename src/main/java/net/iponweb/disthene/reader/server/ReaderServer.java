package net.iponweb.disthene.reader.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import net.iponweb.disthene.reader.config.ReaderConfiguration;
import net.iponweb.disthene.reader.handler.DistheneReaderHandler;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Andrei Ivanov
 */
public class ReaderServer {
    public static final int MAX_CONTENT_LENGTH = 104857600;

    private Logger logger = Logger.getLogger(ReaderServer.class);

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ReaderConfiguration configuration;

    private Map<Pattern, DistheneReaderHandler> handlers = new HashMap<>();

    public ReaderServer(ReaderConfiguration configuration) {
        this.configuration = configuration;
    }

    public void run() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(configuration.getThreads());
        workerGroup = new NioEventLoopGroup(configuration.getThreads());

        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpRequestDecoder(
		            configuration.getMaxInitialLineLength(),
                            configuration.getMaxHeaderSize(),
			    configuration.getMaxChunkSize()
            		));
                        p.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
                        p.addLast(new HttpResponseEncoder());
                        p.addLast(new HttpContentCompressor());
                        p.addLast(new ReaderServerHandler(handlers));
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                        logger.error(cause);
                        super.exceptionCaught(ctx, cause);
                    }
                });

        // Start the server.
        b.bind(configuration.getBind(), configuration.getPort()).sync();
    }

    public void registerHandler(String path, DistheneReaderHandler handler) {
        handlers.put(Pattern.compile(path), handler);
    }

    public void shutdown() {
        logger.info("Shutting down boss group");
        bossGroup.shutdownGracefully().awaitUninterruptibly(60000);

        logger.info("Shutting down worker group");
        workerGroup.shutdownGracefully().awaitUninterruptibly(60000);
    }
}
