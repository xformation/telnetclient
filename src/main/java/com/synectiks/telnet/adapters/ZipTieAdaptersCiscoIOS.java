/**
 * 
 */
package com.synectiks.telnet.adapters;

import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.commons.exceptions.SynectiksException;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.telnet.parsers.CiscoIOSParser;
import com.synectiks.telnet.utils.IRegexUtils;
import com.synectiks.telnet.utils.IZTUtils;

/**
 * @author Rajesh
 */
public class ZipTieAdaptersCiscoIOS extends BaseAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ZipTieAdaptersCiscoIOS.class);

	private String host;
	private int port;

	/**
	 * @param prompt
	 */
	public ZipTieAdaptersCiscoIOS() {
		super(INIT_PROMPT);
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Try to login with default username, passwords
	 * @throws SynectiksException
	 */
	@Override
	public void login() throws SynectiksException {
		login(USER, PASS);
	}

	@Override
	public void login(String username, String password) throws SynectiksException {
		// connect to host
		super.connect(host, port);
		// login with credentials
		super.login(username, password);
		logger.info("Connected and logged-in successfully.");
		// set terminal length 0 to get all response without --MORE--
		super.execute(CMD_TRMNL_LEN_0);
		// set new prompt
		super.setPrompt(PROMPT);
	}

	@Override
	public JSONObject backup() throws SynectiksException {
		JSONObject backup = null;
		try {
			super.isLive();
			String version = super.execute(CMD_VERSION);
			boolean isCat1900 = version.matches("Catalyst (19|28)\\w+");
			String runConfig = getRunningConfig();
			String snmp = super.execute(CMD_SNMP);
			backup = CiscoIOSParser.parseSystem(version, runConfig, snmp);
			String fs = super.execute(CMD_FS);
			JSONObject file_systems = getFileSystems(fs);
			String module = null;
			String power = null;
			String inventory = null;
			String modVer = null;
			String diag = null;
			if (IRegexUtils.isMatch(version,
					"\\bWS-C\\d{4}|Cisco\\s*76\\d{2}", Pattern.CASE_INSENSITIVE)) {
				// on switches and 7600 routers execute "show module"
				module = super.execute(CMD_MODULE, false);
				power = super.execute(CMD_POWER, false);
				inventory = super.execute(CMD_INVENTORY, false);
				modVer = super.execute(CMD_MOD_VER, false);
			} else {
				try {
					diag = super.execute(CMD_DIAGBUG);
				} catch(SynectiksException se) {
					if (se.getMessage().contains("Invalid")) {
						diag = super.execute(CMD_DIAG, false);
					}
				}
			}
			JSONObject chassis = CiscoIOSParser.parseChassis(version, runConfig, snmp, fs, file_systems,
					module, power, inventory, modVer, diag);
			IZTUtils.addInJson(backup, "chassis", chassis);
			String start_config = null;
			if (isCat1900) {
				start_config = super.execute(CMD_START_CONFIG);
			}
			JSONObject config = CiscoIOSParser.createConfig(runConfig, start_config);
			IZTUtils.addInJson(backup, "core:configRepository", config);
			JSONObject accPorts = CiscoIOSParser.parseAccessPorts(runConfig);
			IZTUtils.addInJson(backup, "accessPorts", accPorts);
			String access_lists = super.execute(CMD_ACC_LIST);
			JSONObject filters = CiscoIOSParser.parseFilters(access_lists);
			IZTUtils.addInJson(backup, "filterLists", filters);
			String interfaces = super.execute(CMD_INTERFACES);
			String ospf = super.execute(CMD_IP_OSPF_INTFC);
			JSONObject ifs = CiscoIOSParser.parseInterfaces(runConfig, interfaces, ospf);
			IZTUtils.addInJson(backup, "interfaces", ifs);
			JSONArray lcac = CiscoIOSParser.parseLocalAccounts(runConfig);
			IZTUtils.addInJson(backup, "localAccounts", lcac);
			super.execute(CMD_IP_OPSF);
			String proto = super.execute(CMD_IP_PROTO);
			super.execute(CMD_IP_EIGRP_TOPO);
			JSONObject routing = CiscoIOSParser.parseRouting(runConfig, proto);
			IZTUtils.addInJson(backup, "cisco:routing", routing);
			JSONObject jsnmp = CiscoIOSParser.parseSnmp(runConfig, snmp);
			IZTUtils.addInJson(backup, "snmp", jsnmp);
			String stp = super.execute(CMD_SPAN_TREE);
			JSONArray span = CiscoIOSParser.parseSTP(stp);
			IZTUtils.addInJson(backup, "spanningTree", span);
			JSONArray srout = CiscoIOSParser.parseStaticRoutes(runConfig);
			IZTUtils.addInJson(backup, "staticRoutes", srout);
			String vlans = super.execute(CMD_VLANS);
			JSONArray arrVlans = CiscoIOSParser.parseVlans(vlans);
			IZTUtils.addInJson(backup, "vlans", arrVlans);
			String vtpStatus = super.execute(CMD_VTP_STATUS);
			JSONObject vtp = CiscoIOSParser.parseVtp(vtpStatus);
			IZTUtils.addInJson(backup, "cisco:vlanTrunking", vtp);
		} catch (Throwable th) {
			throw new SynectiksException(th.getMessage(), th);
		} finally {
			// finally disconnect it
			super.disconnect();
		}
		return backup;
	}

	/**
	 * Parse file system response to get the system drives.
	 * @param fs
	 * @return
	 */
	private JSONObject getFileSystems(String fs) {
		JSONObject file_systems = null;
		List<String> fss = IRegexUtils.getMatchingGrops(Pattern.MULTILINE, fs, "^\\*?\\s+\\d+\\s+\\d+.*:");
		if (!IUtils.isNull(fss)) {
			file_systems = new JSONObject();
			for (String s : fss) {
				logger.debug("s: " + s);
				List<String> f = IRegexUtils.getMatchingGrops(s, "^\\*?\\s+\\d+\\s+\\d+\\s+\\b(\\S+)\\b\\s+[A-Za-z]+\\s+(\\S+):");
				logger.debug("f: " + f);
				if (!IRegexUtils.isMatch(f.get(1), "opaque|nvram", Pattern.CASE_INSENSITIVE)) {
					String key = f.get(2);
					String val = null;
					try {
						val = execute("show " + key);
					} catch (SynectiksException e) {
						// ignore it
					}
					if (!IUtils.isNullOrEmpty(key) && !IUtils.isNullOrEmpty(val)) {
						IZTUtils.addInJson(file_systems, key, val);
					}
				}
			}
		}
		return file_systems;
	}

	/**
	 * Method to get running config from device.
	 * @return
	 * @throws SynectiksException
	 */
	private String getRunningConfig() throws SynectiksException {
		super.isLive();
		String runConfig = super.execute(CMD_RUN_CONFIG);
		return runConfig;
	}

}
