package app;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 管理端口Http处理器（目前只有退出存盘）
 *
 */
public class ManagementHttpHandler implements HttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(ManagementHttpHandler.class);

	@Override
	public void handle(HttpExchange he) throws IOException {
		switch (ServerStaticInfo.getServerType()) {
		case "Hall":
			HallServer.stop();
			break;
		case "Game":
			GameServer.stop();
			break;
		case "Center":
			CenterServer.stop();
			break;
		case "GM":
			GMServer.stop();
			break;
		}

		String response = String.format("Close %sServer finished!", ServerStaticInfo.getServerType());
		he.sendResponseHeaders(200, response.length());
		he.getResponseBody().write(response.getBytes());

		he.getRequestBody().close();
		he.getResponseBody().close();
		he.close();

		logger.info(ServerStaticInfo.getServerType() + "Server stop...");

		System.exit(0);
	}

}
