/**
 * 
 */
package com.synectiks.telnet.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.commons.exceptions.SynectiksException;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.telnet.interfaces.IZipTieAdapter;

/**
 * @author Rajesh
 */
public abstract class BaseAdapter implements IZipTieAdapter {

	private static final Logger logger = LoggerFactory.getLogger(BaseAdapter.class);

	public static final String CMD_TRMNL_LEN_0 = "terminal length 0";
	public static final String[] ERR_MSGS = {
			"Invalid input",
			"ERROR: UNRECOGNIZED COMMAND",
			"Unable to get configuration"
	};
	public static final String INIT_PROMPT = ">";
	public static final int PORT = 23;

	private InputStream in;
	private PrintStream out;
	private TelnetClient telnet;
	private String prompt;

	public BaseAdapter(String prompt) {
		this.telnet = new TelnetClient();
		this.prompt = prompt;
	}

	/**
	 * Method to check res for error messages if command failed to execute.
	 * @param res
	 * @param failOnException 
	 * @throws SynectiksException
	 */
	protected void isValidResponse(String res, boolean failOnException) throws SynectiksException {
		if (!IUtils.isNullOrEmpty(res)) {
			for (String err : ERR_MSGS) {
				if (res.contains(err)) {
					if (failOnException) {
						throw new SynectiksException(err);
					} else {
						logger.error(err);
					}
				}
			}
		}
	}

	/**
	 * Check if we are still connected with server
	 * @throws SynectiksException
	 */
	protected void isLive() throws SynectiksException {
		if (IUtils.isNull(telnet) || !telnet.isConnected()) {
			throw new SynectiksException("Connection lost!");
		}
	}

	/**
	 * Method to connect with server and try to login with credentials.
	 * @param host
	 * @param port
	 * @param user
	 * @param pass
	 * @throws SynectiksException
	 */
	protected void connect(String host, int port)
			throws SynectiksException {
		try {
			this.telnet.connect(host, port);
			this.in = this.telnet.getInputStream();
			this.out = new PrintStream(telnet.getOutputStream());
		} catch (IOException e) {
			throw new SynectiksException(e.getMessage(), e);
		}
	}

	@Override
	public void login(String user, String pass) throws SynectiksException {
		if (!IUtils.isNullOrEmpty(user) && !IUtils.isNullOrEmpty(pass)) {
			this.isLive();
			waitFor("sername:");
			write(user);
			waitFor("assword:");
			write(pass);
			waitFor(prompt);
		}
	}

	/**
	 * Method to execute a command and return the response
	 * @param cmd
	 * @param waitFor
	 * @param failOnException
	 * @return
	 * @throws SynectiksException
	 */
	protected String execute(String cmd, String waitFor, boolean failOnException) throws SynectiksException {
		this.isLive();
		if (!IUtils.isNullOrEmpty(cmd)) {
			write(cmd);
		}
		if (!IUtils.isNullOrEmpty(waitFor)) {
			return waitFor(waitFor, failOnException);
		}
		return null;
	}

	/**
	 * Method to execute a command and return the response
	 * @param cmd
	 * @param waitFor
	 * @return
	 * @throws SynectiksException
	 */
	protected String execute(String cmd) throws SynectiksException {
		return execute(cmd, prompt, true);
	}

	/**
	 * Method to execute a command and return the response
	 * @param cmd
	 * @param failOnException
	 * @return
	 * @throws SynectiksException
	 */
	protected String execute(String cmd, boolean failOnException) throws SynectiksException {
		return execute(cmd, prompt, failOnException);
	}

	/**
	 * Method to write a command in out stream
	 * @param cmd
	 * @throws SynectiksException 
	 */
	protected void write(String cmd) throws SynectiksException {
		if (!IUtils.isNullOrEmpty(cmd)) {
			this.isLive();
			out.println(cmd);
			out.flush();
		}
	}

	/**
	 * Method to read the response and wait for string.
	 * Returns the whole steam if string not found.
	 * @param str
	 * @return
	 * @throws SynectiksException
	 */
	protected String waitFor(String str) throws SynectiksException {
		return waitFor(str, true);
	}

	/**
	 * Method to read the response and wait for string.
	 * Returns the whole steam if string not found.
	 * @param str
	 * @param failOnException 
	 * @return
	 * @throws SynectiksException
	 */
	protected String waitFor(String str, boolean failOnException) throws SynectiksException {
		StringBuilder buff = new StringBuilder();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {}
		try {
			int c = in.read();
			while (-1 != c) {
				buff.append((char) c);
				if (buff.indexOf(str) != -1) {
					logger.debug("Found: '" + str + "'");
					break;
				}
				if (in.available() == 0) {
					break;
				}
				c = in.read();
			}
		} catch (IOException ioe) {
			if (failOnException) {
				throw new SynectiksException(ioe.getMessage(), ioe);
			} else {
				logger.error(ioe.getMessage(), ioe);
			}
		}
		logger.warn(str + " not found in stream!");
		logger.info("Prompt: " + getPrompt(buff.toString()));
		// Check for command failure response and throw error
		this.isValidResponse(buff.toString(), failOnException);
		return buff.toString();
	}

	@Override
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	/**
	 * Method to disconnect from server.
	 * @throws SynectiksException
	 */
	protected void disconnect() throws SynectiksException {
		this.isLive();
		try {
			this.telnet.disconnect();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Method to extract prompt from string
	 * @param input
	 * @return
	 */
	public String getPrompt(String input) {
		if (!IUtils.isNullOrEmpty(input)) {
			String[] lines = input.split("\\r?\\n", -1);
			int len = lines.length;
			if (!IUtils.isNull(lines) && len > 0) {
				String prompt = lines[len - 1];
				if (IUtils.isNullOrEmpty(prompt) && len >= 2) {
					prompt = lines[len - 2];
				}
				return prompt;
			}
		}
		return null;
	}

}
