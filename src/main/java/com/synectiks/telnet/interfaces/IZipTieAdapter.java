/**
 * 
 */
package com.synectiks.telnet.interfaces;

import org.codehaus.jettison.json.JSONObject;

import com.synectiks.commons.exceptions.SynectiksException;

/**
 * @author Rajesh
 */
public interface IZipTieAdapter {

	String CMD_VERSION = "show version";
	String CMD_SNMP = "show snmp";
	String CMD_FS = "show file systems";
	String CMD_MODULE = "show module";
	String CMD_POWER = "show power";
	String CMD_INVENTORY = "show inventory";
	String CMD_MOD_VER = "show mod ver";
	String CMD_DIAGBUG = "show diagbus";
	String CMD_DIAG = "show diag";
	String CMD_ACC_LIST = "show access-lists";
	String CMD_INTERFACES = "show interfaces";
	String CMD_IP_OSPF_INTFC = "show ip ospf interface";
	String CMD_IP_OPSF = "show ip ospf";
	String CMD_IP_PROTO = "show ip protocols";
	String CMD_IP_EIGRP_TOPO = "show ip eigrp topology";
	String CMD_SPAN_TREE = "show spanning-tree";
	String CMD_VTP_STATUS = "show vtp status";
	String CMD_VLANS = "show vlan";

	String CMD_RUN_CONFIG = "show running-config";
	String CMD_START_CONFIG = "show startup-config";

	String PROMPT = "#";
	String USER = "testlab";
	String PASS = "hobbit";

	/**
	 * set host ip
	 * @param host
	 */
	public void setHost(String host);

	/**
	 * set host port number
	 * @param port
	 */
	public void setPort(int port);

	/**
	 * Try to login with default username, passwords
	 * @throws SynectiksException
	 */
	public void login() throws SynectiksException;

	/**
	 * Method to start backup operation on device.
	 * @return
	 * @throws SynectiksException
	 */
	public JSONObject backup() throws SynectiksException;

	/**
	 * Method to login into telnet server
	 * @param user
	 * @param pass
	 * @throws SynectiksException
	 */
	public void login(String username, String password) throws SynectiksException;

	/**
	 * Method to set known prompt to read for.
	 * @param prompt
	 */
	public void setPrompt(String prompt);
}
