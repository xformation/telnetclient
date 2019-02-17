/**
 * 
 */
package com.synectiks.telnet.utils;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.synectiks.commons.utils.IUtils;

/**
 * @author Rajesh
 */
public interface IZTUtils {

	enum PROTOCOLS {
		AOL(5190),
		AOL1(5191),
		AOL2(5192),
		AOL3(5193),
		BGP(179),
		BIFF(512),
		BOOTPC(68),
		BOOTPS(67),
		DHCP(547),
		CHARGEN(19),
		CMD(514),
		DAYTIME(13),
		DISCARD(9),
		DNSIX (195),
		DOMAIN(53),
		DNS(53),
		ECHO(7),
		EXEC(512),
		FINGER(79),
		FTP_DATA(20),
		FTP(21),
		GOPHER(70),
		HOSTNAME(101),
		IDENT (113),
		IRC(194),
		ISAKMP(500),
		KLOGIN(543),
		KSHELL(544),
		LDAP(389),
		LDP(646),
		LOGIN (513),
		LPD(515),
		LOTUSNOTES(1352),
		MOBILE_IP(434),
		MSDP(639),
		NAMESERVER(42),
		NETBIOS_DGM(138),
		NETBIOS_NS(137),
		NETBIOS_SS(139),
		NETBIOS_SSN(139),
		NNTP(119),
		NTP(123),
		PIM_AUTO_RP(496),
		POP2(109),
		POP3(110),
		RADIUS(1645),
		RADIUS_ACCT(1646),
		RIP(520),
		SMTP(25),
		SNMP(161),
		SNMPTRAP(162),
		SSH(22),
		SSL(443),
		SUNRPC(111),
		SYSLOG(514),
		TACACS(49),
		TALK(517),
		TELNET(23),
		TFTP(69),
		TIME(37),
		UUCP(540),
		WHO(513),
		WHOIS (43),
		WWW(80),
		HTTP(80),
		HTTPS (443),
		XDMCP(177);
	
		private int port;
		
		private PROTOCOLS(int port) {
			this.port = port;
		}
		
		public int getPort() {
			return this.port;
		}
	}

	/**
	 * Method to add key values into json
	 * @param cards
	 * @param key
	 * @param val
	 */
	static void addInJson(JSONObject cards, String key, Object val) {
		try {
			cards.put(key, val);
		} catch (JSONException e) {
			// ignore it
		}
	}

	/**
	 * Method to identify the interface type
	 * @param type
	 * @return
	 */
	static String getInterfaceType(String type) {
		String res = "unknown";
		if (!IUtils.isNullOrEmpty(type)) {
			if (type.toLowerCase().matches("loopback")) {
				res = "softwareLoopback";
			} else if (type.toLowerCase().matches("frame")) {
				res = "frameRelay";
			} else if (type.toLowerCase().matches("eth")) {
				res = "ethernet";
			} else if (type.toLowerCase().matches("token")) {
				res = "tokenRing";
			} else if (type.toLowerCase().matches("BRI|PRI")) {
				res = "isdn";
			} else if (type.toLowerCase().matches("(gre|isdn|other|modem|ppp|serial|atm|sonet)")) {
				res = IRegexUtils.getFirstGrop(type, "(gre|isdn|other|modem|ppp|serial|atm|sonet)");
			} else {
				res = "other";
			}
		}
		return res;
	}

	/**
	 * Method to mask ip in bit i.e. translates 255.255.255.0 into 24
	 * @param val
	 * @return
	 */
	static String maskToBit(String val) {
		if (!IUtils.isNullOrEmpty(val)) {
			String[] arr = val.split("\\.");
			if (!IUtils.isNull(arr) && arr.length >= 4) {
				long result = 0;
				for (int i = 0; i < 4; i++) {
					int power = 3 - i;
					result += ((Integer.parseInt(arr[i]) % 256 * Math.pow(256, power)));
				}
				System.out.println(val + " => " + result);
				int mask = 1;
				int counter = 0;
				for (int i = 0; i < 32; i++) {
					if ((result & mask) != 0) {
						counter ++;
					}
					mask <<= 1;
				}
				System.out.println("Counter => " + counter);
				return String.valueOf(counter);
			}
		}
		return val;
	}

	/**
	 * Method to strips out everything excluding hex chars
	 * @param mac
	 * @return
	 */
	static String stripMac(String mac) {
		if (!IUtils.isNullOrEmpty(mac)) {
			mac = mac.replaceAll("[^0-9a-f]", "");
			return mac.toUpperCase();
		}
		return null;
	}

	/**
	 * Method to convert given a short name like "Fa3/15"
	 * return "FastEthernet3/15"
	 * @param in
	 * @return
	 */
	static String getFullPort(String in) {
		if (!IUtils.isNullOrEmpty(in)) {
			in = in.trim();
			if (in.startsWith("Fa")) return in.replace("Fa", "FastEthernet");
			if (in.startsWith("Eth")) return in.replace("Eth", "Ethernet");
			if (in.startsWith("Gig")) return in.replace("Gig", "GigabitEthernet");
		}
		return null;
	}

}
