/**
 * 
 */
package com.synectiks.telnet.controllers;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synectiks.commons.utils.IUtils;
import com.synectiks.telnet.adapters.BaseAdapter;
import com.synectiks.telnet.factory.AdapterFactory;
import com.synectiks.telnet.interfaces.IZipTieAdapter;

/**
 * @author Rajesh
 */
@RestController
@RequestMapping(path = "/telnetclient", method = RequestMethod.GET)
@CrossOrigin
public class TelnetController {

	private static final Logger logger = LoggerFactory.getLogger(TelnetController.class);

	/**
	 * /telnetclient/backup api to collect backup object for specified ip.
	 * @param cls Adapter class name either as
	 * "<code>ZipTie::Adapters::Cisco::IOS<code>" or as
	 * "<code>ZipTieAdaptersCiscoIOS<code>"
	 * @param ip device ip i.e. 0.0.0.0
	 * @param port device port
	 * @param user device login username
	 * @param password device login password
	 * @param prompt device active prompt after login
	 * @return
	 */
	@RequestMapping("/backup")
	public ResponseEntity<Object> backup(
			@RequestParam(name = "cls") String cls,
			@RequestParam(name = "ip") String ip,
			@RequestParam(name = "port", required = false) Integer port,
			@RequestParam(name = "user", required = false) String user,
			@RequestParam(name = "password", required = false) String password,
			@RequestParam(name = "prompt", required = false) String prompt) {
		String res = null;
		try {
			if (IUtils.isNull(port) || port == 0) {
				port = BaseAdapter.PORT;
			}
			IZipTieAdapter adapter = AdapterFactory.getInstance().getAdapter(cls);
			adapter.setHost(ip);
			adapter.setPort(port);
			if (!IUtils.isNullOrEmpty(prompt)) {
				adapter.setPrompt(prompt);
			}
			if (!IUtils.isNullOrEmpty(user) && !IUtils.isNullOrEmpty(password)) {
				adapter.login(user, password);
			} else {
				adapter.login();
			}
			JSONObject obj = adapter.backup();
			if (!IUtils.isNull(obj)) {
				res = obj.toString();
			}
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<>(IUtils.getFailedResponse(ex),
					HttpStatus.PRECONDITION_FAILED);
		}
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
}
