package net.iponweb.disthene.reader;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import net.iponweb.disthene.reader.services.CassandraService;
import net.iponweb.disthene.reader.services.PathsService;
import org.apache.log4j.Logger;

/**
 * Hello world!
 */
public class Main {
    final static Logger logger = Logger.getLogger(Main.class);
    private static int port = 9080;
    private static Channel channel = null;
    private static EventLoopGroup bossGroup = new NioEventLoopGroup(10);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup(10);

    public static void main(String[] args) throws Exception {
        logger.info("Starting connection to ES");
        PathsService.getInstance();
        logger.info("Done");

        logger.info("Starting connection to C*");
        CassandraService.getInstance();
        logger.info("Done");


        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new DisthenServerInitializer());

        channel = serverBootstrap.bind(port).sync().channel();
        logger.info("Listening on port " + port);
    }

    public static void shutdown() {
        ChannelFuture f = channel.close();
        f.awaitUninterruptibly();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
