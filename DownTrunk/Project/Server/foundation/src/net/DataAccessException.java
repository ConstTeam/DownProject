package net;

import org.apache.commons.lang.ArrayUtils;

public class DataAccessException extends Exception {
	private static final long serialVersionUID = 1L;
	public static final int INTERNAL_CLIENT_ERROR = 400;
	public static final int SEVER_REDIRECT_ERROR = 302;
	public static final int REQUEST_REFUSED = 401;
	public static final int CLIENT_ACCESS_ERROR = 402;
	public static final int CLIENT_IO_ERROR = 403;
	public static final int CLIENT_DATA_ERROR = 404;
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int SERVER_TIMEOUT = 501;
	public static final int SERVER_ACCESS_ERROR = 502;
	public static final int SERVER_IO_ERROR = 503;
	public static final int SERVER_DATA_ERROR = 504;
	private int type = 0;

	private String address = null;

	private String[] messages = null;

	public static String typeMessage(int type) {
		switch (type) {
		case 400:
			return "INTERNAL_CLIENT_ERROR";
		case 302:
			return "SEVER_REDIRECT_ERROR";
		case 401:
			return "REQUEST_REFUSED";
		case 402:
			return "CLIENT_ACCESS_ERROR";
		case 403:
			return "CLIENT_IO_ERROR";
		case 504:
			return "SERVER_DATA_ERROR";
		case 500:
			return "INTERNAL_SERVER_ERROR";
		case 501:
			return "SERVER_TIMEOUT";
		case 502:
			return "SERVER_ACCESS_ERROR";
		case 503:
			return "SERVER_IO_ERROR";
		case 404:
			return "CLIENT_DATA_ERROR";
		}
		return null;
	}

	public DataAccessException(int type, String message) {
		super(message);
		if (type < 300)
			throw new IllegalArgumentException("invalid type");
		this.type = type;
	}

	public DataAccessException(int type, String message, String... paras) {
		this(type, message);
		this.messages = paras;
	}

	public int getType() {
		return this.type;
	}

	public String getTypeMessage() {
		return typeMessage(this.type);
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String toString() {
		String str = typeMessage(this.type);
		if (str == null)
			str = Integer.toString(this.type);
		String args = "";
		if (this.messages != null) {
			args = ArrayUtils.toString(this.messages);
		}
		return getClass().getName() + ":" + str + ", address=" + this.address
				+ ", " + getMessage() + " " + args;
	}

	public String[] getMessages() {
		return this.messages;
	}

	public void setMessages(String[] messages) {
		this.messages = messages;
	}
}