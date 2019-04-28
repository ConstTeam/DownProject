package net;

import io.netty.channel.ChannelHandlerContext;

public abstract class WebServicelet implements IWebService {
	protected IContext context;

	public void doMessage(ChannelHandlerContext channel, IByteBuffer data) {
		try {
			access(channel, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getBackletInfo() {
		return getClass().getName();
	}

	public IContext getContext() {
		return this.context;
	}

	public void init(IContext context) {
		this.context = context;
	}

	public void destroy() {
	}

	public abstract void access(ChannelHandlerContext channel,
			IByteBuffer paramIByteBuffer) throws DataAccessException;
}