package app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import message.web.WebServiceConfig;
import net.IWebService;
import net.WebServiceManager;
import net.WebSocketServerHandler;

public class WebSocketServer {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
	
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private WebSocketServer() {};
	
	private void run(int port, WebServiceConfig webServiceConfig) throws Exception {

		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();

		try {

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							WebSocketServerHandler webSocketServerHandler = new WebSocketServerHandler();
							WebServiceManager serviceManager = new WebServiceManager();

							// 初始化并设置场景service
							HashMap<Integer, Class<?>> clientServices = webServiceConfig.getServices();
							Iterator<Entry<Integer, Class<?>>> iterator = clientServices.entrySet().iterator();
							while (iterator.hasNext()) {
								Entry<Integer, Class<?>> next = iterator.next();
								int moduleId = next.getKey();
								IWebService service = (IWebService) next.getValue().newInstance();
								serviceManager.addService(moduleId, service);
							}
							webSocketServerHandler.setWebServiceManager(serviceManager);

							pipeline.addLast("http-codec", new HttpServerCodec());
							pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
							ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
							pipeline.addLast("handler", webSocketServerHandler);
						}
					});

			Channel ch = b.bind(port).sync().channel();
			logger.debug("Web socket server started at port {}.", port);
			logger.debug("Open your browser and navigate to http://localhost:{}/", port);

			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	/**
	 * WebSocket启动
	 *  
	 * @throws Exception
	 */
	public static void startGameWebService() throws Exception {
		WebSocketServer webSocketServer = new WebSocketServer();
		ServerManager.addWebSocketServer(ServerStaticInfo.getServerType(), webSocketServer);
		webSocketServer.run(ServerStaticInfo.getAddress(ServerStaticInfo.GAME_INTERNET).getPort(), WebServiceConfig.getInstance());
	}
	
	public void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
}