package net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Ä¬ÈÏÏìÓ¦ */
public class DefaultResponse extends Response {

	Logger log = LoggerFactory.getLogger(getClass());

	public DefaultResponse() {
		super(-1);
	}

	@Override
	public void respondFail(IByteBuffer data) {
		log.debug("respondFail: " + data.available());
	}

	@Override
	public void respondOK(IByteBuffer data) {
		log.debug("responseOK: " + data.available());
	}

}
