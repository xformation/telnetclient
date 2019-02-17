package com.synectiks.telnet.utils;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

import org.apache.commons.net.telnet.TelnetClient;
import org.codehaus.jettison.json.JSONObject;

import com.synectiks.telnet.parsers.CiscoIOSParser;

@SuppressWarnings("unused")
public class TelnetUtils {
	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	private static String prompt = ">";

	public TelnetUtils(String server, String user, String password) {
		try {
			// Connect to the specified server
			telnet.connect(server, 23);

			// Get input and output stream references
			in = telnet.getInputStream();
			out = new PrintStream(telnet.getOutputStream());

			// Log the user on
			readUntil("sername:");
			write(user);
			readUntil("assword:");
			write(password);

			// Advance to a prompt
			readUntil(prompt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void su(String password) {
		try {
			write("su");
			readUntil("Password: ");
			write(password);
			prompt = "#";
			readUntil(prompt + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String readUntil(String pattern) {
		StringBuffer sb = new StringBuffer();
		try {
			char lastChar = pattern.charAt(pattern.length() - 1);
			char ch = (char) in.read();
			while (true) {
				System.out.print(ch);
				sb.append(ch);
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						break;
					}
				}
				if (in.available() == 0) {
					break;
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void write(String value) {
		try {
			out.println(value);
			out.flush();
			System.out.println(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String sendCommand(String command) {
		try {
			write(command);
			return readUntil(prompt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		try {
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			String version = "Cisco IOS Software, C3750 Software (C3750-ADVIPSERVICESK9-M), Version 12.2(25)SEE4, RELEASE SOFTWARE (fc1)\r\n" + 
					"Copyright (c) 1986-2007 by Cisco Systems, Inc.\r\n" + 
					"Compiled Mon 16-Jul-07 03:24 by myl\r\n" + 
					"Image text-base: 0x00003000, data-base: 0x01280000\r\n" + 
					"\r\n" + 
					"ROM: Bootstrap program is C3750 boot loader\r\n" + 
					"BOOTLDR: C3750 Boot Loader (C3750-HBOOT-M) Version 12.2(25r)SEC, RELEASE SOFTWARE (fc4)\r\n" + 
					"\r\n" + 
					"labcore1 uptime is 1 year, 10 weeks, 3 days, 13 hours, 37 minutes\r\n" + 
					"System returned to ROM by power-on\r\n" + 
					"System restarted at 03:09:29 UTC Fri Aug 17 2007\r\n" + 
					"System image file is \"flash:c3750-advipservicesk9-mz.122-25.SEE4.bin\"\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"This product contains cryptographic features and is subject to United\r\n" + 
					"States and local country laws governing import, export, transfer and\r\n" + 
					"use. Delivery of Cisco cryptographic products does not imply\r\n" + 
					"third-party authority to import, export, distribute or use encryption.\r\n" + 
					"Importers, exporters, distributors and users are responsible for\r\n" + 
					"compliance with U.S. and local country laws. By using this product you\r\n" + 
					"agree to comply with applicable laws and regulations. If you are unable\r\n" + 
					"to comply with U.S. and local laws, return this product immediately.\r\n" + 
					"\r\n" + 
					"A summary of U.S. laws governing Cisco cryptographic products may be found at:\r\n" + 
					"http://www.cisco.com/wwl/export/crypto/tool/stqrg.html\r\n" + 
					"\r\n" + 
					"If you require further assistance please contact us by sending email to\r\n" + 
					"export@cisco.com.\r\n" + 
					"\r\n" + 
					"cisco WS-C3750G-24T (PowerPC405) processor (revision L0) with 118784K/12280K bytes of memory.\r\n" + 
					"Processor board ID CAT1012Z5E3\r\n" + 
					"Last reset from power-on\r\n" + 
					"19 Virtual Ethernet interfaces\r\n" + 
					"24 Gigabit Ethernet interfaces\r\n" + 
					"The password-recovery mechanism is enabled.\r\n" + 
					"\r\n" + 
					"512K bytes of flash-simulated non-volatile configuration memory.\r\n" + 
					"Base ethernet MAC Address       : 00:17:94:45:EE:80\r\n" + 
					"Motherboard assembly number     : 73-9679-09\r\n" + 
					"Power supply part number        : 341-0048-03\r\n" + 
					"Motherboard serial number       : CAT10120NS8\r\n" + 
					"Power supply serial number      : LIT10070A98\r\n" + 
					"Model revision number           : L0\r\n" + 
					"Motherboard revision number     : A0\r\n" + 
					"Model number                    : WS-C3750G-24T-E\r\n" + 
					"System serial number            : CAT1012Z5E3\r\n" + 
					"Top Assembly Part Number        : 800-26633-01\r\n" + 
					"Top Assembly Revision Number    : C0\r\n" + 
					"Version ID                      : V05\r\n" + 
					"CLEI Code Number                : COMR200BRA\r\n" + 
					"Hardware Board Revision Number  : 0x02\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"Switch   Ports  Model              SW Version              SW Image            \r\n" + 
					"------   -----  -----              ----------              ----------          \r\n" + 
					"*    1   24     WS-C3750G-24T      12.2(25)SEE4            C3750-ADVIPSERVICESK\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"Configuration register is 0xF\r\n";
			String snmp = "Chassis: CAT1012Z5E3\r\n" + 
					"Contact: EricBasham\r\n" + 
					"73397038 SNMP packets input\r\n" + 
					"    0 Bad SNMP version errors\r\n" + 
					"    259020 Unknown community name\r\n" + 
					"    0 Illegal operation for community name supplied\r\n" + 
					"    45 Encoding errors\r\n" + 
					"    135554663 Number of requested variables\r\n" + 
					"    0 Number of altered variables\r\n" + 
					"    21677200 Get-request PDUs\r\n" + 
					"    51444816 Get-next PDUs\r\n" + 
					"    0 Set-request PDUs\r\n" + 
					"73137973 SNMP packets output\r\n" + 
					"    0 Too big errors (Maximum packet size 1500)\r\n" + 
					"    9921 No such name errors\r\n" + 
					"    0 Bad values errors\r\n" + 
					"    0 General errors\r\n" + 
					"    73132781 Response PDUs\r\n" + 
					"    0 Trap PDUs\r\n" + 
					"SNMP global trap: disabled\r\n" + 
					"\r\n" + 
					"SNMP logging: disabled\r\n" + 
					"SNMP agent enabled\\r\\n";
			String runningConfig = "Building configuration...\r\n" + 
					"\r\n" + 
					"Current configuration : 10150 bytes\r\n" + 
					"!\r\n" + 
					"! Last configuration change at 10:36:11 UTC Wed Aug 27 2008 by security\r\n" + 
					"! NVRAM config last updated at 10:35:41 UTC Wed Aug 27 2008 by security\r\n" + 
					"!\r\n" + 
					"version 12.2\r\n" + 
					"no service pad\r\n" + 
					"service timestamps debug uptime\r\n" + 
					"service timestamps log datetime\r\n" + 
					"service password-encryption\r\n" + 
					"service sequence-numbers\r\n" + 
					"!\r\n" + 
					"hostname WAN1\r\n" + 
					"!\r\n" + 
					"enable secret 5 $1$QJM5$FzpSSkUoBf7WEEMjYKsF5.\r\n" + 
					"!\r\n" + 
					"username mantest password 7 110B0B0003060D0D163C2E\r\n" + 
					"username security password 7 010503035A18040E2355\r\n" + 
					"username testlab privilege 14 password 7 000C1C0406521F\r\n" + 
					"aaa new-model\r\n" + 
					"aaa authentication login default group tacacs+ local\r\n" + 
					"aaa authentication enable default group tacacs+ enable\r\n" + 
					"aaa authorization commands 15 default group tacacs+ local \r\n" + 
					"aaa accounting commands 15 default start-stop group tacacs+\r\n" + 
					"!\r\n" + 
					"aaa session-id common\r\n" + 
					"clock timezone UTC -6\r\n" + 
					"clock summer-time UTC recurring\r\n" + 
					"switch 1 provision ws-c3750g-24t\r\n" + 
					"ip subnet-zero\r\n" + 
					"ip routing\r\n" + 
					"ip domain-name alterpoint.com\r\n" + 
					"ip name-server 10.10.1.9\r\n" + 
					"!\r\n" + 
					"ip ssh version 1\r\n" + 
					"ipv6 unicast-routing\r\n" + 
					"!\r\n" + 
					"!\r\n" + 
					"crypto pki trustpoint TP-self-signed-2487611008\r\n" + 
					" enrollment selfsigned\r\n" + 
					" subject-name cn=IOS-Self-Signed-Certificate-2487611008\r\n" + 
					" revocation-check none\r\n" + 
					" rsakeypair TP-self-signed-2487611008\r\n" + 
					"!\r\n" + 
					"!\r\n" + 
					"crypto ca certificate chain TP-self-signed-2487611008\r\n" + 
					" certificate self-signed 01\r\n" + 
					"  3082029C 30820205 A0030201 02020101 300D0609 2A864886 F70D0101 04050030 \r\n" + 
					"  58312F30 2D060355 04031326 494F532D 53656C66 2D536967 6E65642D 43657274 \r\n" + 
					"  69666963 6174652D 32343837 36313130 30383125 30230609 2A864886 F70D0109 \r\n" + 
					"  0216166C 6162636F 72652E61 6C746572 706F696E 742E636F 6D301E17 0D393330 \r\n" + 
					"  33303130 30303134 325A170D 32303031 30313030 30303030 5A305831 2F302D06 \r\n" + 
					"  03550403 1326494F 532D5365 6C662D53 69676E65 642D4365 72746966 69636174 \r\n" + 
					"  652D3234 38373631 31303038 31253023 06092A86 4886F70D 01090216 166C6162 \r\n" + 
					"  636F7265 2E616C74 6572706F 696E742E 636F6D30 819F300D 06092A86 4886F70D \r\n" + 
					"  01010105 0003818D 00308189 02818100 CF1005C7 B583F345 51DA9FF4 F223B11B \r\n" + 
					"  FB10EA67 5FD284DF 1E16005E 9485129C A5A97696 715356AD 54DCE9D3 D638A83D \r\n" + 
					"  B832E414 BB4024EC 3E55BB09 7391089A BBE96DCF 04CB69F4 0AA1CDA2 7EAB9CCC \r\n" + 
					"  285889E9 03FED0CA 8F763E8B 9E67FB58 FB9E2FF9 C4DB2001 E6A3AC12 AE589CF0 \r\n" + 
					"  7C506C2E 799D4C25 1040817A 2161FCBF 02030100 01A37630 74300F06 03551D13 \r\n" + 
					"  0101FF04 05300301 01FF3021 0603551D 11041A30 1882166C 6162636F 72652E61 \r\n" + 
					"  6C746572 706F696E 742E636F 6D301F06 03551D23 04183016 80149C61 436BB3AC \r\n" + 
					"  FAC2A903 201A4B46 6A9FE05C EDEB301D 0603551D 0E041604 149C6143 6BB3ACFA \r\n" + 
					"  C2A90320 1A4B466A 9FE05CED EB300D06 092A8648 86F70D01 01040500 03818100 \r\n" + 
					"  21A8A5B0 3C1A2E01 56F44F9C 6C07F8C6 F35E9D7C BD0D2EC1 4BD22E47 94E8DB75 \r\n" + 
					"  8D416E14 D72E6743 0825B04A A5FC658C 7BCAC7B4 BB2444D6 EFA9A479 11546421 \r\n" + 
					"  EC7CD4FB FF346AEF 6BAD8225 006A600C C82EB62E 00EC61C7 FF4FEBE5 24E21B84 \r\n" + 
					"  7A2FB3AA B3A2AF76 CA25290D 06563656 2D23CAAE 2425389F 8865BE7A E064E333\r\n" + 
					"  quit\r\n" + 
					"!\r\n" + 
					"!\r\n" + 
					"no file verify auto\r\n" + 
					"!\r\n" + 
					"spanning-tree mode pvst\r\n" + 
					"spanning-tree extend system-id\r\n" + 
					"spanning-tree vlan 1,20,100,201-205,215,221-225,230-231 priority 24576\r\n" + 
					"!\r\n" + 
					"vlan internal allocation policy ascending\r\n" + 
					"!\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/1\r\n" + 
					" ip address 10.100.100.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/2\r\n" + 
					" ip address 10.100.101.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/3\r\n" + 
					" ip address 10.100.102.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/4\r\n" + 
					" ip address 10.100.103.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/5\r\n" + 
					" ip address 10.100.104.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/6\r\n" + 
					" ip address 10.100.105.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/7\r\n" + 
					" ip address 10.100.106.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/8\r\n" + 
					" ip address 10.100.107.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/9\r\n" + 
					" ip address 10.100.108.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/10\r\n" + 
					" ip address 10.100.109.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/11\r\n" + 
					" ip address 10.100.110.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/12\r\n" + 
					" ip address 10.100.111.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/13\r\n" + 
					" ip address 10.100.112.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/14\r\n" + 
					" ip address 10.100.113.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/15\r\n" + 
					" ip address 10.100.114.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/16\r\n" + 
					" ip address 10.100.115.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/17\r\n" + 
					" ip address 10.100.116.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/18\r\n" + 
					" ip address 10.100.117.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/19\r\n" + 
					" ip address 10.100.118.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/20\r\n" + 
					" ip address 10.100.119.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/21\r\n" + 
					" ip address 10.100.120.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/22\r\n" + 
					" ip address 10.100.121.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/23\r\n" + 
					" ip address 10.100.122.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"interface GigabitEthernet1/0/24\r\n" + 
					" ip address 10.100.123.1 255.255.255.0\r\n" + 
					"!\r\n" + 
					"router ospf 100\r\n" + 
					" log-adjacency-changes\r\n" + 
					" network 10.100.20.0 0.0.0.255 area 0\r\n" + 
					" default-information originate\r\n" + 
					"!\r\n" + 
					"ip classless\r\n" + 
					"no ip http server\r\n" + 
					"no ip http secure-server\r\n" + 
					"!\r\n" + 
					"ip tacacs source-interface Vlan100\r\n" + 
					"!\r\n" + 
					"logging trap notifications\r\n" + 
					"logging facility local0\r\n" + 
					"logging source-interface GigabitEthernet1/0/24\r\n" + 
					"logging 10.100.32.107\r\n" + 
					"logging 10.100.32.43\r\n" + 
					"access-list 10 deny   169.254.0.0 0.0.255.255 log\r\n" + 
					"access-list 10 permit any\r\n" + 
					"access-list 15 remark Created for Proxy Experiment with Brent\r\n" + 
					"access-list 15 permit 10.10.1.89\r\n" + 
					"access-list 15 permit 10.100.32.0 0.0.0.255\r\n" + 
					"access-list 15 permit 192.168.0.0 0.0.255.255\r\n" + 
					"access-list 40 permit 10.100.32.0 0.0.0.255\r\n" + 
					"access-list 40 remark snmp access\r\n" + 
					"access-list 110 deny   ip any host 192.168.2.254 log\r\n" + 
					"access-list 110 deny   ip any host 192.168.2.253 log\r\n" + 
					"access-list 110 permit ip 10.100.19.0 0.0.0.255 any\r\n" + 
					"access-list 110 permit ip 10.100.32.0 0.0.1.255 any\r\n" + 
					"access-list 110 permit ip 10.100.0.0 0.0.31.255 10.0.0.0 0.255.255.255\r\n" + 
					"access-list 110 permit ip 10.100.0.0 0.0.31.255 192.168.0.0 0.0.255.255\r\n" + 
					"access-list 110 permit ip 10.20.0.0 0.0.255.255 any\r\n" + 
					"access-list 110 permit ip host 10.100.3.16 any\r\n" + 
					"access-list 110 permit ip 10.100.7.0 0.0.0.255 any\r\n" + 
					"!\r\n" + 
					"ipv6 router ospf 100\r\n" + 
					" log-adjacency-changes\r\n" + 
					" redistribute rip ripper\r\n" + 
					"!\r\n" + 
					"ipv6 router rip ripper\r\n" + 
					" redistribute connected\r\n" + 
					"!\r\n" + 
					"snmp-server community Testing RO\r\n" + 
					"snmp-server community c0r4 RO\r\n" + 
					"snmp-server community public RO\r\n" + 
					"snmp-server contact EricBasham\r\n" + 
					"tacacs-server host 10.100.32.130\r\n" + 
					"tacacs-server directed-request\r\n" + 
					"tacacs-server key 7 15010E0F1138223031\r\n" + 
					"radius-server source-ports 1645-1646\r\n" + 
					"!\r\n" + 
					"control-plane\r\n" + 
					"!\r\n" + 
					"banner motd ^C\r\n" + 
					"WARNING!!!\r\n" + 
					"This system is solely for the use of authorized users for official purposes.\r\n" + 
					"You have no expectation of privacy in its use and to ensure that the system\r\n" + 
					"is functioning properly, individuals using this computer system are subject\r\n" + 
					"to having all of their activities monitored and recorded by system\r\n" + 
					"personnel.  Use of this system evidences an express consent to such\r\n" + 
					"monitoring and agreement that if such monitoring reveals evidence of\r\n" + 
					"possible abuse or criminal activity, system personnel may provide the\r\n" + 
					"results of such monitoring to appropriate officials.\r\n" + 
					"^C\r\n" + 
					"privilege exec level 14 copy running-config tftp\r\n" + 
					"privilege exec level 14 copy startup-config tftp\r\n" + 
					"!\r\n" + 
					"line con 0\r\n" + 
					"line vty 5 15\r\n" + 
					"!\r\n" + 
					"ntp clock-period 36029303\r\n" + 
					"ntp source Vlan100\r\n" + 
					"ntp server 192.43.244.18\r\n" + 
					"ntp server 129.9.15.2\r\n" + 
					"end\r\n" + 
					"";
			String fs = "File Systems:\r\n" + 
					"\r\n" + 
					"     Size(b)     Free(b)      Type  Flags  Prefixes\r\n" + 
					"*   15998976     1722368     flash     rw   flash: flash1:\r\n" + 
					"           -           -    opaque     rw   vb:\r\n" + 
					"           -           -    opaque     ro   bs:\r\n" + 
					"           -           -    opaque     rw   system:\r\n" + 
					"      524288      512655     nvram     rw   nvram:\r\n" + 
					"           -           -    opaque     ro   xmodem:\r\n" + 
					"           -           -    opaque     ro   ymodem:\r\n" + 
					"           -           -    opaque     rw   null:\r\n" + 
					"           -           -   network     rw   tftp:\r\n" + 
					"           -           -   network     rw   rcp:\r\n" + 
					"           -           -   network     rw   http:\r\n" + 
					"           -           -   network     rw   ftp:\r\n" + 
					"           -           -   network     rw   scp:\r\n" + 
					"           -           -   network     rw   https:\r\n" + 
					"           -           -    opaque     ro   cns:\r\n" + 
					"";
			String module = "\r\n" +
					"Mod Ports Card Type                              Model              Serial No.\r\n" + 
					"--- ----- -------------------------------------- ------------------ -----------\r\n" + 
					"  1    2  Catalyst 6000 supervisor 2 (Active)    WS-X6K-S2U-MSFC2   SAL08445F8B\r\n" + 
					"  2    8  8 port 1000mb ethernet                 WS-X6408-GBIC      SAD0409037P\r\n" + 
					"  3   48  48 port 10/100 mb RJ45                 WS-X6348-RJ-45     SAD043306HV\r\n" + 
					"  6    6  Firewall Module                        WS-SVC-FWM-1       SAD0948024S\r\n" + 
					"\r\n" + 
					"Mod MAC addresses                       Hw    Fw           Sw           Status\r\n" + 
					"--- ---------------------------------- ------ ------------ ------------ -------\r\n" + 
					"  1  0009.1247.fd58 to 0009.1247.fd59   5.1   7.1(1)       12.2(17d)SXB Ok\r\n" + 
					"  2  00b0.c2f0.78b0 to 00b0.c2f0.78b7   2.7   5.4(2)       8.3(0.110)TE Ok\r\n" + 
					"  3  0001.6411.de1c to 0001.6411.de4b   1.1   5.4(2)       8.3(0.110)TE Ok\r\n" + 
					"  6  0015.6214.d9dc to 0015.6214.d9e3   4.0   7.2(1)       3.1(4)0      Ok\r\n" + 
					"\r\n" + 
					"Mod Sub-Module                  Model              Serial        Hw     Status \r\n" + 
					"--- --------------------------- ------------------ ------------ ------- -------\r\n" + 
					"  1 Policy Feature Card 2       WS-F6K-PFC2        SAL08455V6V   3.5    Ok\r\n" + 
					"  1 Cat6k MSFC 2 daughterboard  WS-F6K-MSFC2       SAL08455TJ8   2.8    Ok\r\n" + 
					"\r\n" + 
					"Mod Online Diag Status \r\n" + 
					"--- -------------------\r\n" + 
					"  1 Pass\r\n" + 
					"  2 Pass\r\n" + 
					"  3 Pass\r\n" + 
					"  6 Pass\r\n" + 
					"";
			String power = "show power\r\n" + 
					"system power redundancy mode = redundant\r\n" + 
					"system power total =     1153.32 Watts (27.46 Amps @ 42V)\r\n" + 
					"system power used =       556.92 Watts (13.26 Amps @ 42V)\r\n" + 
					"system power available =  596.40 Watts (14.20 Amps @ 42V)\r\n" + 
					"                        Power-Capacity PS-Fan Output Oper\r\n" + 
					"PS   Type               Watts   A @42V Status Status State\r\n" + 
					"---- ------------------ ------- ------ ------ ------ -----\r\n" + 
					"1    WS-CAC-1300W       1153.32 27.46  OK     OK     on \r\n" + 
					"2    WS-CAC-1300W       1153.32 27.46  OK     OK     on \r\n" + 
					"                        Pwr-Requested  Pwr-Allocated  Admin Oper\r\n" + 
					"Slot Card-Type          Watts   A @42V Watts   A @42V State State\r\n" + 
					"---- ------------------ ------- ------ ------- ------ ----- -----\r\n" + 
					"1    WS-X6K-S2U-MSFC2    142.38  3.39   142.38  3.39  on    on\r\n" + 
					"2    WS-X6408-GBIC       142.38  3.39   142.38  3.39  on    on\r\n" + 
					"3    WS-X6348-RJ-45      100.38  2.39   100.38  2.39  on    on\r\n" + 
					"6    WS-SVC-FWM-1        171.78  4.09   171.78  4.09  on    on\r\n" + 
					"";
			String inventory = "show inventory\r\n" + 
					"NAME: \"WS-C6506\", DESCR: \"Cisco Systems Catalyst 6500 6-slot Chassis System\"\r\n" + 
					"PID: WS-C6506          , VID:    , SN: SAL08290JNU\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6K-VTT 1\", DESCR: \"VTT 1\"\r\n" + 
					"PID: WS-C6K-VTT        , VID:    , SN: SMT0826A845\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6K-VTT 2\", DESCR: \"VTT 2\"\r\n" + 
					"PID: WS-C6K-VTT        , VID:    , SN: SMT0826C533\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6K-VTT 3\", DESCR: \"VTT 3\"\r\n" + 
					"PID: WS-C6K-VTT        , VID:    , SN: SMT0826C515\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6000-CL\", DESCR: \"C6k Clock 1\"\r\n" + 
					"PID: WS-C6000-CL       , VID:    , SN: SMT0827D056\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6000-CL\", DESCR: \"C6k Clock 2\"\r\n" + 
					"PID: WS-C6000-CL       , VID:    , SN: SMT0827D046\r\n" + 
					"\r\n" + 
					"NAME: \"1\", DESCR: \"WS-X6K-S2U-MSFC2 2 ports Catalyst 6000 supervisor 2 Rev. 5.1\"\r\n" + 
					"PID: WS-X6K-S2U-MSFC2  , VID:    , SN: SAL08445F8B\r\n" + 
					"\r\n" + 
					"NAME: \"sub-module of 1\", DESCR: \"WS-F6K-MSFC2 Cat6k MSFC 2 daughterboard Rev. 2.8\"\r\n" + 
					"PID: WS-F6K-MSFC2      , VID:    , SN: SAL08455TJ8\r\n" + 
					"\r\n" + 
					"NAME: \"sub-module of 1\", DESCR: \"WS-F6K-PFC2 Policy Feature Card 2 Rev. 3.5\"\r\n" + 
					"PID: WS-F6K-PFC2       , VID:    , SN: SAL08455V6V\r\n" + 
					"\r\n" + 
					"NAME: \"2\", DESCR: \"WS-X6408-GBIC 8 port 1000mb ethernet Rev. 2.7\"\r\n" + 
					"PID: WS-X6408-GBIC     , VID:    , SN: SAD0409037P\r\n" + 
					"\r\n" + 
					"NAME: \"3\", DESCR: \"WS-X6348-RJ-45 48 port 10/100 mb RJ45 Rev. 1.1\"\r\n" + 
					"PID: WS-X6348-RJ-45    , VID:    , SN: SAD043306HV\r\n" + 
					"\r\n" + 
					"NAME: \"6\", DESCR: \"WS-SVC-FWM-1 6 ports Firewall Module Rev. 4.0\"\r\n" + 
					"PID: WS-SVC-FWM-1      , VID:    , SN: SAD0948024S\r\n" + 
					"\r\n" + 
					"NAME: \"PS 1 WS-CAC-1300W\", DESCR: \"110/220v AC power supply, 1360 watt 1\"\r\n" + 
					"PID: WS-CAC-1300W      , VID:    , SN: ACP03291041\r\n" + 
					"\r\n" + 
					"NAME: \"PS 2 WS-CAC-1300W\", DESCR: \"110/220v AC power supply, 1360 watt 2\"\r\n" + 
					"PID: WS-CAC-1300W      , VID:    , SN: ACP04060158\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"AUS-6506#show inventory\r\n" + 
					"	show inventory\r\n" + 
					"NAME: \"WS-C6506\", DESCR: \"Cisco Systems Catalyst 6500 6-slot Chassis System\"\r\n" + 
					"PID: WS-C6506          , VID:    , SN: SAL08290JNU\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6K-VTT 1\", DESCR: \"VTT 1\"\r\n" + 
					"PID: WS-C6K-VTT        , VID:    , SN: SMT0826A845\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6K-VTT 2\", DESCR: \"VTT 2\"\r\n" + 
					"PID: WS-C6K-VTT        , VID:    , SN: SMT0826C533\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6K-VTT 3\", DESCR: \"VTT 3\"\r\n" + 
					"PID: WS-C6K-VTT        , VID:    , SN: SMT0826C515\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6000-CL\", DESCR: \"C6k Clock 1\"\r\n" + 
					"PID: WS-C6000-CL       , VID:    , SN: SMT0827D056\r\n" + 
					"\r\n" + 
					"NAME: \"WS-C6000-CL\", DESCR: \"C6k Clock 2\"\r\n" + 
					"PID: WS-C6000-CL       , VID:    , SN: SMT0827D046\r\n" + 
					"\r\n" + 
					"NAME: \"1\", DESCR: \"WS-X6K-S2U-MSFC2 2 ports Catalyst 6000 supervisor 2 Rev. 5.1\"\r\n" + 
					"PID: WS-X6K-S2U-MSFC2  , VID:    , SN: SAL08445F8B\r\n" + 
					"\r\n" + 
					"NAME: \"sub-module of 1\", DESCR: \"WS-F6K-MSFC2 Cat6k MSFC 2 daughterboard Rev. 2.8\"\r\n" + 
					"PID: WS-F6K-MSFC2      , VID:    , SN: SAL08455TJ8\r\n" + 
					"\r\n" + 
					"NAME: \"sub-module of 1\", DESCR: \"WS-F6K-PFC2 Policy Feature Card 2 Rev. 3.5\"\r\n" + 
					"PID: WS-F6K-PFC2       , VID:    , SN: SAL08455V6V\r\n" + 
					"\r\n" + 
					"NAME: \"2\", DESCR: \"WS-X6408-GBIC 8 port 1000mb ethernet Rev. 2.7\"\r\n" + 
					"PID: WS-X6408-GBIC     , VID:    , SN: SAD0409037P\r\n" + 
					"\r\n" + 
					"NAME: \"3\", DESCR: \"WS-X6348-RJ-45 48 port 10/100 mb RJ45 Rev. 1.1\"\r\n" + 
					"PID: WS-X6348-RJ-45    , VID:    , SN: SAD043306HV\r\n" + 
					"\r\n" + 
					"NAME: \"6\", DESCR: \"WS-SVC-FWM-1 6 ports Firewall Module Rev. 4.0\"\r\n" + 
					"PID: WS-SVC-FWM-1      , VID:    , SN: SAD0948024S\r\n" + 
					"\r\n" + 
					"NAME: \"PS 1 WS-CAC-1300W\", DESCR: \"110/220v AC power supply, 1360 watt 1\"\r\n" + 
					"PID: WS-CAC-1300W      , VID:    , SN: ACP03291041\r\n" + 
					"\r\n" + 
					"NAME: \"PS 2 WS-CAC-1300W\", DESCR: \"110/220v AC power supply, 1360 watt 2\"\r\n" + 
					"PID: WS-CAC-1300W      , VID:    , SN: ACP04060158\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"";
			String modVer = "show mod ver\r\n" + 
					"               ^\r\n" + 
					"% Invalid input detected at '^' marker.\r\n" + 
					"\r\n";
			String diag = "show diag\r\n" + 
					"Slot 0:\r\n" + 
					"	C2811 Motherboard with 2FE and integrated VPN Port adapter, 2 ports\r\n" + 
					"	Port adapter is analyzed \r\n" + 
					"	Port adapter insertion time unknown\r\n" + 
					"	Onboard VPN		: v2.3.3\r\n" + 
					"	EEPROM contents at hardware discovery:\r\n" + 
					"	PCB Serial Number        : FOC093644K2\r\n" + 
					"	Hardware Revision        : 3.0\r\n" + 
					"	Top Assy. Part Number    : 800-21849-02\r\n" + 
					"	Board Revision           : B0\r\n" + 
					"	Deviation Number         : 0\r\n" + 
					"	Fab Version              : 06\r\n" + 
					"	RMA Test History         : 00\r\n" + 
					"	RMA Number               : 0-0-0-0\r\n" + 
					"	RMA History              : 00\r\n" + 
					"	Processor type           : 87 \r\n" + 
					"	Hardware date code       : 20050912\r\n" + 
					"	Chassis Serial Number    : FTX0942A3L9\r\n" + 
					"	Chassis MAC Address      : 0015.6295.6948\r\n" + 
					"	MAC Address block size   : 24\r\n" + 
					"	CLEI Code                : CNMJ7N0BRA\r\n" + 
					"	Product (FRU) Number     : CISCO2811      \r\n" + 
					"	Part Number              : 73-7214-10\r\n" + 
					"	Version Identifier       : V01 \r\n" + 
					"	EEPROM format version 4\r\n" + 
					"	EEPROM contents (hex):\r\n" + 
					"	  0x00: 04 FF C1 8B 46 4F 43 30 39 33 36 34 34 4B 32 40\r\n" + 
					"	  0x10: 03 E7 41 03 00 C0 46 03 20 00 55 59 02 42 42 30\r\n" + 
					"	  0x20: 88 00 00 00 00 02 06 03 00 81 00 00 00 00 04 00\r\n" + 
					"	  0x30: 09 87 83 01 31 F3 E0 C2 8B 46 54 58 30 39 34 32\r\n" + 
					"	  0x40: 41 33 4C 39 C3 06 00 15 62 95 69 48 43 00 18 C6\r\n" + 
					"	  0x50: 8A 43 4E 4D 4A 37 4E 30 42 52 41 CB 8F 43 49 53\r\n" + 
					"	  0x60: 43 4F 32 38 31 31 20 20 20 20 20 20 82 49 1C 2E\r\n" + 
					"	  0x70: 0A 89 56 30 31 20 D9 02 40 C1 FF FF FF FF FF FF\r\n" + 
					"\r\n" + 
					"	WIC Slot 0:\r\n" + 
					"	4 Port FE Switch\r\n" + 
					"	Base MAC Address         : 0015.63a0.b8c0\r\n" + 
					"	MAC Address block size   : 4\r\n" + 
					"	PCB Serial Number        : FOC09380F0F\r\n" + 
					"	Hardware Revision        : 5.1\r\n" + 
					"	Part Number              : 73-8474-05\r\n" + 
					"	Board Revision           : B0\r\n" + 
					"	Top Assy. Part Number    : 800-24193-01\r\n" + 
					"	Deviation Number         : 0\r\n" + 
					"	Fab Version              : 05\r\n" + 
					"	CLEI Code                : IPUIAE0RAA\r\n" + 
					"	RMA Test History         : 00\r\n" + 
					"	RMA Number               : 0-0-0-0\r\n" + 
					"	RMA History              : 00\r\n" + 
					"	Product (FRU) Number     : HWIC-4ESW\r\n" + 
					"	Version Identifier       : VN/A\r\n" + 
					"	Connector Type           : 01\r\n" + 
					"	EEPROM format version 4\r\n" + 
					"	EEPROM contents (hex):\r\n" + 
					"	  0x00: 04 FF CF 06 00 15 63 A0 B8 C0 43 00 04 C1 8B 46\r\n" + 
					"	  0x10: 4F 43 30 39 33 38 30 46 30 46 40 00 56 41 05 01\r\n" + 
					"	  0x20: 82 49 21 1A 05 42 42 30 C0 46 03 20 00 5E 81 01\r\n" + 
					"	  0x30: 88 00 00 00 00 02 05 C6 8A 49 50 55 49 41 45 30\r\n" + 
					"	  0x40: 52 41 41 03 00 81 00 00 00 00 04 00 CB 89 48 57\r\n" + 
					"	  0x50: 49 43 2D 34 45 53 57 89 56 4E 2F 41 D9 02 C1 40\r\n" + 
					"	  0x60: 05 01 FF FF FF FF FF FF FF FF FF FF FF FF FF FF\r\n" + 
					"	  0x70: FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF\r\n" + 
					"";
			String diagbus = "show diagbus\r\n" + 
					"    Versions\r\n" + 
					"--- ---- ------------------ ----------- --------------------------------------\r\n" + 
					" 1    2  WS-X6K-S2U-MSFC2   SAL08445F8B Hw : 5.1\r\n" + 
					"                                        Fw : 7.1(1)\r\n" + 
					"                                        Sw : 12.2(17d)SXB10\r\n" + 
					"                                        Fw1: 6.1(3) \r\n" + 
					"                                        Sw1: 8.3(0.110)TET15 \r\n" + 
					"         WS-F6K-MSFC2       SAL08455TJ8 Hw : 2.8\r\n" + 
					"                                        Fw : 12.2(17r)S1\r\n" + 
					"                                        Sw : 12.2(17d)SXB10\r\n" + 
					"         WS-F6K-PFC2        SAL08455V6V Hw : 3.5\r\n" + 
					" 2    8  WS-X6408-GBIC      SAD0409037P Hw : 2.7\r\n" + 
					"                                        Fw : 5.4(2) \r\n" + 
					"                                        Sw : 8.3(0.110)TET15 \r\n" + 
					" 3   48  WS-X6348-RJ-45     SAD043306HV Hw : 1.1\r\n" + 
					"                                        Fw : 5.4(2) \r\n" + 
					"                                        Sw : 8.3(0.110)TET15 \r\n" + 
					" 6    6  WS-SVC-FWM-1       SAD0948024S Hw : 4.0\r\n" + 
					"                                        Fw : 7.2(1) \r\n" + 
					"                                        Sw : 3.1(4)0\r\n" + 
					"";
			String interfaces = "show interfaces\r\n" + 
					"GigabitEthernet1/0/1 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0000.0000 (bia 1000.0000.0000)\r\n" + 
					"  Internet address is 10.100.100.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:00, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 785000 bits/sec, 571 packets/sec\r\n" + 
					"  5 minute output rate 420000 bits/sec, 342 packets/sec\r\n" + 
					"     2544829834 packets input, 2692224904 bytes, 0 no buffer\r\n" + 
					"     Received 3190124444 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     1 input errors, 1 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 4234282268 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     3524282293 packets output, 3885910301 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/2 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0001.0001 (bia 1000.0001.0001)\r\n" + 
					"  Internet address is 10.100.101.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:01, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 3000 bits/sec, 5 packets/sec\r\n" + 
					"     14242334 packets input, 3474376677 bytes, 0 no buffer\r\n" + 
					"     Received 55914 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 0 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     133323949 packets output, 1425608662 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/3 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0002.0002 (bia 1000.0002.0002)\r\n" + 
					"  Internet address is 10.100.102.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:01, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 80000 bits/sec, 5 packets/sec\r\n" + 
					"     3302506442 packets input, 1344543141 bytes, 0 no buffer\r\n" + 
					"     Received 138319 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 3515 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     10783412 packets output, 856246672 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/4 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0003.0003 (bia 1000.0003.0003)\r\n" + 
					"  Internet address is 10.100.103.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:01, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 1000 bits/sec, 1 packets/sec\r\n" + 
					"  5 minute output rate 3000 bits/sec, 5 packets/sec\r\n" + 
					"     88832289 packets input, 240051837 bytes, 0 no buffer\r\n" + 
					"     Received 249011 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 0 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     200639406 packets output, 3035394596 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/5 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0004.0004 (bia 1000.0004.0004)\r\n" + 
					"  Internet address is 10.100.104.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, medshow ip ospf interface\r\n" + 
					"ia type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:01, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 3000 bits/sec, 5 packets/sec\r\n" + 
					"     3717967131 packets input, 3554545570 bytes, 0 no buffer\r\n" + 
					"     Received 2048231 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 151 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     2432789290 packets output, 3659182712 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/6 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0005.0005 (bia 1000.0005.0005)\r\n" + 
					"  Internet address is 10.100.105.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:01, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 3000 bits/sec, 5 packets/sec\r\n" + 
					"     1450474001 packets input, 3508236556 bytes, 0 no buffer\r\n" + 
					"     Received 45760 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 46 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     244486421 packets output, 2455544977 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/7 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0006.0006 (bia 1000.0006.0006)\r\n" + 
					"  Internet address is 10.100.106.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 5000 bits/sec, 2 packets/sec\r\n" + 
					"  5 minute output rate 12000 bits/sec, 5 packets/sec\r\n" + 
					"     37398303 packets input, 4166357838 bytes, 0 no buffer\r\n" + 
					"     Received 307718 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 16652 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     164882743 packets output, 4145401737 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/8 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0007.0007 (bia 1000.0007.0007)\r\n" + 
					"  Internet address is 10.100.107.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"    show ip ospf\r\n" + 
					" reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 3000 bits/sec, 5 packets/sec\r\n" + 
					"     563760523 packets input, 1463664996 bytes, 0 no buffer\r\n" + 
					"     Received 111906 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 5 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     871635547 packets output, 2248425065 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/9 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0008.0008 (bia 1000.0008.0008)\r\n" + 
					"  Internet address is 10.100.108.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:01, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 2000 bits/sec, 4 packets/sec\r\n" + 
					"  5 minute output rate 28000 bits/sec, 16 packets/sec\r\n" + 
					"     171655474 packets input, 258948219 bytes, 0 no buffer\r\n" + 
					"     Received 1937242 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 24 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     320948624 packets output, 1952182237 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/10 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0009.0009 (bia 1000.0009.0009)\r\n" + 
					"  Internet address is 10.100.109.1/24\r\n" + 
					"  MTU 1500 bytes, BW 100000 Kbit, DLY 100 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 100Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 00:00:01, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 3000 bits/sec, 5 packets/sec\r\n" + 
					"     556994689 packets input, 3951474507 bytes, 0 no buffer\r\n" + 
					"     Received 144867 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 3460 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     669104251 packets output, 1851846612 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/11 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0010.0010 (bia 1000.0010.0010)\r\n" + 
					"  Internet address is 10.100.110.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:21, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 27000 bits/sec, 41 packets/sec\r\n" + 
					"     17579850 packets input, 941082362 bytes, 0 no buffer\r\n" + 
					"     Received 2229431 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 1960814 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     4170078606 packets output, 1396378822 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/12 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0011.0011 (bia 1000.0011.0011)\r\n" + 
					"  Internet address is 10.100.111.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:15, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 217000 bits/sec, 94 packets/sec\r\n" + 
					"  5 minute output rate 158000 bits/sec, 177 packets/sec\r\n" + 
					"     1709286517 packets input, 3755017314 bytes, 0 no buffer\r\n" + 
					"     Received 5912925 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 2010128 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     1819562844 packets output, 1856822441 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/13 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0012.0012 (bia 1000.0012.0012)\r\n" + 
					"  Internet address is 10.100.112.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:21, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 26000 bits/sec, 40 packets/sec\r\n" + 
					"     1379273548 packets input, 2345746837 bytes, 0 no buffer\r\n" + 
					"     Received 2397206 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     5 input errors, 5 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 1958609 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     1007481999 packets output, 2237354132 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/14 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0013.0013 (bia 1000.0013.0013)\r\n" + 
					"  Internet address is 10.100.113.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:10, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 1259000 bits/sec, 716 packets/sec\r\n" + 
					"  5 minute output rate 3411000 bits/sec, 888 packets/sec\r\n" + 
					"     2951831760 packets input, 2849835066 bytes, 0 no buffer\r\n" + 
					"     Received 12440113 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 2298865 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     2567122028 packets output, 1452296681 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/15  is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0014.0014 (bia 1000.0014.0014)\r\n" + 
					"  Internet address is 10.100.114.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 35w5d, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 0 bits/sec, 0 packets/sec\r\n" + 
					"     39074651 packets input, 279714989 bytes, 0 no buffer\r\n" + 
					"     Received 144340 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 137334 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     57015019 packets output, 3541835685 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/16  is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0015.0015 (bia 1000.0015.0015)\r\n" + 
					"  Internet address is 10.100.115.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Auto-duplex, Auto-speed, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output 35w5d, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 0 bits/sec, 0 packets/sec\r\n" + 
					"     102 packets input, 12017 bytes, 0 no buffer\r\n" + 
					"     Received 102 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 5 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     23 packets output, 1506 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/17 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0016.0016 (bia 1000.0016.0016)\r\n" + 
					"  Internet address is 10.100.116.1/24\r\n" + 
					"  MTU 1500 bytes, BW 100000 Kbit, DLY 100 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 100Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:08:07, output 00:00:04, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 0 bits/sec, 0 packets/sec\r\n" + 
					"     3634111 packets input, 542233677 bytes, 0 no buffer\r\n" + 
					"     Received 20079 broadcasts (0 IP multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 0 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     6671776 packets output, 682057751 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 0 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/18 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0017.0017 (bia 1000.0017.0017)\r\n" + 
					"  Internet address is 10.100.117.1/24\r\n" + 
					"  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Half-duplex, 10Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:13:24, output 00:00:04, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 0 bits/sec, 0 packets/sec\r\n" + 
					"     1257609 packets input, 275781193 bytes, 0 no buffer\r\n" + 
					"     Received 21893 broadcasts (0 IP multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 0 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     4270033 packets output, 456514432 bytes, 0 underruns\r\n" + 
					"     0 output errors, 4749 collisions, 0 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/19 is  is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0018.0018 (bia 1000.0018.0018)\r\n" + 
					"  Internet address is 10.100.118.1/24\r\n" + 
					"  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Auto-duplex, Auto-speed, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input never, output never, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 0 bits/sec, 0 packets/sec\r\n" + 
					"  5 minute output rate 0 bits/sec, 0 packets/sec\r\n" + 
					"     0 packets input, 0 bytes, 0 no buffer\r\n" + 
					"     Received 0 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 0 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     0 packets output, 0 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/20 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0019.0019 (bia 1000.0019.0019)\r\n" + 
					"  Internet address is 10.100.119.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:03, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 166000 bits/sec, 63 packets/sec\r\n" + 
					"  5 minute output rate 231000 bits/sec, 134 packets/sec\r\n" + 
					"     1352069197 packets input, 735939209 bytes, 0 no buffer\r\n" + 
					"     Received 3009351 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 2261541 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     1122767387 packets output, 2782250554 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/21 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0020.0020 (bia 1000.0020.0020)\r\n" + 
					"  Internet address is 10.100.120.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:04, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 4000 bits/sec, 5 packets/sec\r\n" + 
					"  5 minute output rate 28000 bits/sec, 44 packets/sec\r\n" + 
					"     695881826 packets input, 159080556 bytes, 0 no buffer\r\n" + 
					"     Received 9033891 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 2214047 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     630178098 packets output, 2775242180 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/22 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0021.0021 (bia 1000.0021.0021)\r\n" + 
					"  Internet address is 10.100.121.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:24, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 2774000 bits/sec, 354 packets/sec\r\n" + 
					"  5 minute output rate 275000 bits/sec, 264 packets/sec\r\n" + 
					"     3496327289 packets input, 2581880224 bytes, 0 no buffer\r\n" + 
					"     Received 5610932 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 1954681 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     123248395 packets output, 1955851236 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/23 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0022.0022 (bia 1000.0022.0022)\r\n" + 
					"  Internet address is 10.100.122.1/24\r\n" + 
					"  MTU 1500 bytes, BW 100000 Kbit, DLY 100 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 100Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:00, output 00:00:00, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 8000 bits/sec, 13 packets/sec\r\n" + 
					"  5 minute output rate 19000 bits/sec, 27 packets/sec\r\n" + 
					"     469606450 packets input, 4187940382 bytes, 0 no buffer\r\n" + 
					"     Received 468536846 broadcasts (0 multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     3 input errors, 3 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 467913472 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     3694175054 packets output, 3277559597 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 1 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"GigabitEthernet1/0/24 is up, line protocol is up (connected)\r\n" + 
					"  Hardware is Gigabit Ethernet, address is 1000.0023.0023 (bia 1000.0023.0023)\r\n" + 
					"  Internet address is 10.100.123.1/24\r\n" + 
					"  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec, \r\n" + 
					"     reliability 255/255, txload 1/255, rxload 1/255\r\n" + 
					"  Encapsulation ARPA, loopback not set\r\n" + 
					"  Keepalive set (10 sec)\r\n" + 
					"  Full-duplex, 1000Mb/s, media type is 10/100/1000BaseTX\r\n" + 
					"  input flow-control is off, output flow-control is unsupported \r\n" + 
					"  ARP type: ARPA, ARP Timeout 04:00:00\r\n" + 
					"  Last input 00:00:00, output 00:00:03, output hang never\r\n" + 
					"  Last clearing of \"show interface\" counters never\r\n" + 
					"  Input queue: 0/75/5543/0 (size/max/drops/flushes); Total output drops: 0\r\n" + 
					"  Queueing strategy: fifo\r\n" + 
					"  Output queue: 0/40 (size/max)\r\n" + 
					"  5 minute input rate 3830000 bits/sec, 3784 packets/sec\r\n" + 
					"  5 minute output rate 4472000 bits/sec, 3850 packets/sec\r\n" + 
					"     4088024877 packets input, 4200392992 bytes, 0 no buffer\r\n" + 
					"     Received 4854629 broadcasts (0 IP multicast)\r\n" + 
					"     0 runts, 0 giants, 0 throttles\r\n" + 
					"     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\r\n" + 
					"     0 watchdog, 4853101 multicast, 0 pause input\r\n" + 
					"     0 input packets with dribble condition detected\r\n" + 
					"     440410625 packets output, 2187069331 bytes, 0 underruns\r\n" + 
					"     0 output errors, 0 collisions, 0 interface resets\r\n" + 
					"     0 babbles, 0 late collision, 0 deferred\r\n" + 
					"     0 lost carrier, 0 no carrier, 0 PAUSE output\r\n" + 
					"     0 output buffer failures, 0 output buffers swapped out\r\n" + 
					"labcore1#";
			String ipProto = "show ip protocols\r\n" + 
					"*** IP Routing is NSF aware ***\r\n" + 
					"\r\n" + 
					"Routing Protocol is \"ospf 100\"\r\n" + 
					"  Outgoing update filter list for all interfaces is not set\r\n" + 
					"  Incoming update filter list for all interfaces is not set\r\n" + 
					"  Router ID 10.100.100.1\r\n" + 
					"  It is an autonomous system boundary router\r\n" + 
					"  Redistributing External Routes from,\r\n" + 
					"  Number of areas in this router is 1. 1 normal 0 stub 0 nssa\r\n" + 
					"  Maximum path: 4\r\n" + 
					"  Routing for Networks:\r\n" + 
					"    10.100.20.0 0.0.0.255 area 0\r\n" + 
					"  Routing Information Sources:\r\n" + 
					"    Gateway         Distance      Last Update\r\n" + 
					"    12.12.12.13          110      35w5d\r\n" + 
					"    10.100.100.1         110      6d03h\r\n" + 
					"    10.100.20.82         110      6d03h\r\n" + 
					"    10.100.32.1          110      6d03h\r\n" + 
					"    10.100.20.18         110      6d03h\r\n" + 
					"    10.100.5.4           110      38w6d\r\n" + 
					"    10.100.29.1          110      25w5d\r\n" + 
					"    10.100.28.1          110      4w6d\r\n" + 
					"    10.100.26.1          110      6d03h\r\n" + 
					"    10.100.20.227        110      6d03h\r\n" + 
					"    10.100.20.224        110      6d03h\r\n" + 
					"    10.100.20.225        110      6d03h\r\n" + 
					"    10.100.20.217        110      6d03h\r\n" + 
					"    10.100.20.222        110      6d03h\r\n" + 
					"    10.100.20.220        110      6d03h\r\n" + 
					"    10.100.20.221        110      6d03h\r\n" + 
					"    10.100.20.210        110      6d03h\r\n" + 
					"    10.100.20.211        110      6d03h\r\n" + 
					"    10.100.20.214        110      6d03h\r\n" + 
					"    10.100.20.215        110      6d03h\r\n" + 
					"    10.100.20.212        110      6d03h\r\n" + 
					"    10.100.20.213        110      6d03h\r\n" + 
					"    10.217.3.2           110      23w5d\r\n" + 
					"  Distance: (default is 110)\r\n" + 
					"\r\n" + 
					"labcore1#";
			String spanTree = "show spanning-tree\r\n" + 
					"\r\n" + 
					"VLAN0001\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24577\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24577  (priority 24576 sys-id-ext 1)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0020\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24596\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24596  (priority 24576 sys-id-ext 20)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0030\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c342\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32798  (priority 32768 sys-id-ext 30)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0031\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c343\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32799  (priority 32768 sys-id-ext 31)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0032\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c344\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32800  (priority 32768 sys-id-ext 32)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0033\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c345\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32801  (priority 32768 sys-id-ext 33)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---show vtp status\r\n" + 
					"- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0034\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c346\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32802  (priority 32768 sys-id-ext 34)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0035\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c347\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32803  (priority 32768 sys-id-ext 35)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0036\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c348\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32804  (priority 32768 sys-id-ext 36)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0037\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c349\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32805  (priority 32768 sys-id-ext 37)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0038\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34a\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32806  (priority 32768 sys-id-ext 38)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0039\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34b\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32807  (priority 32768 sys-id-ext 39)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0040\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34c\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32808  (priority 32768 sys-id-ext 40)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0041\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34d\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32809  (priority 32768 sys-id-ext 41)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0042\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34e\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32810  (priority 32768 sys-id-ext 42)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0043\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34f\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32811  (priority 32768 sys-id-ext 43)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0044\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c350\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32812  (priority 32768 sys-id-ext 44)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0045\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c351\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32813  (priority 32768 sys-id-ext 45)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0046\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c352\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32814  (priority 32768 sys-id-ext 46)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0047\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34a\r\n" + 
					"             Cost        94\r\n" + 
					"             Port        1 (GigabitEthernet1/0/1)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32815  (priority 32768 sys-id-ext 47)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Root FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0048\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c34a\r\n" + 
					"             Cost        94\r\n" + 
					"             Port        1 (GigabitEthernet1/0/1)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32816  (priority 32768 sys-id-ext 48)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Root FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0050\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c369\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32818  (priority 32768 sys-id-ext 50)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0080\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c353\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32848  (priority 32768 sys-id-ext 80)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0081\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c348\r\n" + 
					"             Cost        103\r\n" + 
					"             Port        1 (GigabitEthernet1/0/1)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32849  (priority 32768 sys-id-ext 81)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Root FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0082\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c36b\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32850  (priority 32768 sys-id-ext 82)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0083\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c36c\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32851  (priority 32768 sys-id-ext 83)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0084\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c36d\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32852  (priority 32768 sys-id-ext 84)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0089\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c354\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32857  (priority 32768 sys-id-ext 89)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0100\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24676\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24676  (priority 24576 sys-id-ext 100)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/2          Desg FWD 4         128.2    P2p \r\n" + 
					"Gi1/0/3          Desg FWD 4         128.3    P2p \r\n" + 
					"Gi1/0/4          Desg FWD 4         128.4    P2p \r\n" + 
					"Gi1/0/5          Desg FWD 4         128.5    P2p \r\n" + 
					"Gi1/0/6          Desg FWD 4         128.6    P2p \r\n" + 
					"Gi1/0/7          Desg FWD 4         128.7    P2p \r\n" + 
					"Gi1/0/8          Desg FWD 4         128.8    P2p \r\n" + 
					"Gi1/0/9          Desg FWD 4         128.9    P2p \r\n" + 
					"Gi1/0/10         Desg FWD 19        128.10   P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0200\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c356\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32968  (priority 32768 sys-id-ext 200)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0201\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24777\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24777  (priority 24576 sys-id-ext 201)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0202\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24778\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24778  (priority 24576 sys-id-ext 202)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0203\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24779\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24779  (priority 24576 sys-id-ext 203)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0204\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24780\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24780  (priority 24576 sys-id-ext 204)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0205\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24781\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24781  (priority 24576 sys-id-ext 205)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0206\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c35c\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32974  (priority 32768 sys-id-ext 206)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0207\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c35d\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32975  (priority 32768 sys-id-ext 207)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0208\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    32768\r\n" + 
					"             Address     0001.63bb.c35e\r\n" + 
					"             Cost        19\r\n" + 
					"             Port        23 (GigabitEthernet1/0/23)\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    32976  (priority 32768 sys-id-ext 208)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Root FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0215\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24791\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24791  (priority 24576 sys-id-ext 215)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0221\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    420\r\n" + 
					"             Address     0060.cf48.1ec0\r\n" + 
					"             Cost        42\r\n" + 
					"             Port        1 (GigabitEthernet1/0/1)\r\n" + 
					"             Hello Time   1 sec  Max Age 14 sec  Forward Delay  6 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24797  (priority 24576 sys-id-ext 221)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Root FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0222\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24798\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24798  (priority 24576 sys-id-ext 222)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0223\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24799\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24799  (priority 24576 sys-id-ext 223)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0224\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24800\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24800  (priority 24576 sys-id-ext 224)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0225\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24801\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24801  (priority 24576 sys-id-ext 225)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0230\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24806\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24806  (priority 24576 sys-id-ext 230)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"\r\n" + 
					"VLAN0231\r\n" + 
					"  Spanning tree enabled protocol ieee\r\n" + 
					"  Root ID    Priority    24807\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             This bridge is the root\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"\r\n" + 
					"  Bridge ID  Priority    24807  (priority 24576 sys-id-ext 231)\r\n" + 
					"             Address     0017.9445.ee80\r\n" + 
					"             Hello Time   2 sec  Max Age 20 sec  Forward Delay 15 sec\r\n" + 
					"             Aging Time 300\r\n" + 
					"\r\n" + 
					"Interface        Role Sts Cost      Prio.Nbr Type\r\n" + 
					"---------------- ---- --- --------- -------- --------------------------------\r\n" + 
					"Gi1/0/1          Desg FWD 4         128.1    P2p \r\n" + 
					"Gi1/0/11         Desg FWD 4         128.11   P2p \r\n" + 
					"Gi1/0/12         Desg FWD 4         128.12   P2p \r\n" + 
					"Gi1/0/13         Desg FWD 4         128.13   P2p \r\n" + 
					"Gi1/0/14         Desg FWD 4         128.14   P2p \r\n" + 
					"Gi1/0/20         Desg FWD 4         128.20   P2p \r\n" + 
					"Gi1/0/21         Desg FWD 4         128.21   P2p \r\n" + 
					"Gi1/0/22         Desg FWD 4         128.22   P2p \r\n" + 
					"Gi1/0/23         Desg FWD 19        128.23   P2p \r\n" + 
					"\r\n" + 
					"labcore1#";
			String vtp = "show vtp status\r\n" + 
					"VTP Version                     : 2\r\n" + 
					"Configuration Revision          : 58\r\n" + 
					"Maximum VLANs supported locally : 1005\r\n" + 
					"Number of existing VLANs        : 50\r\n" + 
					"VTP Operating Mode              : Server\r\n" + 
					"VTP Domain Name                 : alterpoint-lab\r\n" + 
					"VTP Pruning Mode                : Disabled\r\n" + 
					"VTP V2 Mode                     : Disabled\r\n" + 
					"VTP Traps Generation            : Disabled\r\n" + 
					"MD5 digest                      : 0x41 0x4E 0x24 0xC1 0x82 0x55 0xC2 0x4B \r\n" + 
					"Configuration last modified by 10.20.0.1 at 5-15-08 18:11:27\r\n" + 
					"Local updater ID is 10.20.0.1 on interface Vl20 (lowest numbered VLAN interface found)\r\n" + 
					"labcore1#";
			String ospf = "show ip ospf\r\n" + 
					" Routing Process \"ospf 100\" with ID 10.100.20.211\r\n" + 
					" Start time: 00:02:20.856, Time elapsed: 21w4d\r\n" + 
					" Supports only single TOS(TOS0) routes\r\n" + 
					" Supports opaque LSA\r\n" + 
					" Supports Link-local Signaling (LLS)\r\n" + 
					" Supports area transit capability\r\n" + 
					" Router is not originating router-LSAs with maximum metric\r\n" + 
					" Initial SPF schedule delay 5000 msecs\r\n" + 
					" Minimum hold time between two consecutive SPFs 10000 msecs\r\n" + 
					" Maximum wait time between two consecutive SPFs 10000 msecs\r\n" + 
					" Incremental-SPF disabled\r\n" + 
					" Minimum LSA interval 5 secs\r\n" + 
					" Minimum LSA arrival 1000 msecs\r\n" + 
					" LSA group pacing timer 240 secs\r\n" + 
					" Interface flood pacing timer 33 msecs\r\n" + 
					" Retransmission pacing timer 66 msecs\r\n" + 
					" Number of external LSA 24. Checksum Sum 0x0C2502\r\n" + 
					" Number of opaque AS LSA 0. Checksum Sum 0x000000\r\n" + 
					" Number of DCbitless external and opaque AS LSA 0\r\n" + 
					" Number of DoNotAge external and opaque AS LSA 0\r\n" + 
					" Number of areas in this router is 1. 1 normal 0 stub 0 nssa\r\n" + 
					" Number of areas transit capable is 0\r\n" + 
					" External flood list length 0\r\n" + 
					" IETF NSF helper support enabled\r\n" + 
					" Cisco NSF helper support enabled\r\n" + 
					" Reference bandwidth unit is 100 mbps\r\n" + 
					"    Area BACKBONE(0)\r\n" + 
					"	Number of interfaces in this area is 6 (1 loopback)\r\n" + 
					"	Area has no authentication\r\n" + 
					"	SPF algorithm last executed 6d03h ago\r\n" + 
					"	SPF algorithm executed 176 times\r\n" + 
					"	Area ranges are\r\n" + 
					"	Number of LSA 45. Checksum Sum 0x189221\r\n" + 
					"	Number of opaque link LSA 0. Checksum Sum 0x000000\r\n" + 
					"	Number of DCbitless LSA 8\r\n" + 
					"	Number of indication LSA 0\r\n" + 
					"	Number of DoNotAge LSA 0\r\n" + 
					"	Flood list length 0\r\n" + 
					"\r\n" + 
					"6506modular#";
			String ipInterface = "show ip ospf interface\r\n" + 
					"Loopback0 is up, line protocol is up \r\n" + 
					"  Internet Address 10.100.20.211/32, Area 0 \r\n" + 
					"  Process ID 100, Router ID 10.100.20.211, Network Type LOOPBACK, Cost: 1\r\n" + 
					"  Loopback interface is treated as a stub Host\r\n" + 
					"FastEthernet1/5 is up, line protocol is up (connected)\r\n" + 
					"  Internet Address 10.100.20.105/30, Area 0 \r\n" + 
					"  Process ID 100, Router ID 10.100.20.211, Network Type BROADCAST, Cost: 1\r\n" + 
					"  Transmit Delay is 1 sec, State DR, Priority 1\r\n" + 
					"  Designated Router (ID) 10.100.20.211, Interface address 10.100.20.105\r\n" + 
					"  Backup Designated router (ID) 10.100.20.220, Interface address 10.100.20.106\r\n" + 
					"  Timer intervals configured, Hello 10, Dead 40, Wait 40, Retransmit 5\r\n" + 
					"    oob-resync timeout 40\r\n" + 
					"    Hello due in 00:00:08\r\n" + 
					"  Supports Link-local Signaling (LLS)\r\n" + 
					"  Cisco NSF helper support enabled\r\n" + 
					"  IETF NSF helper support enabled\r\n" + 
					"  Index 5/5, flood queue length 0\r\n" + 
					"  Next 0x0(0)/0x0(0)\r\n" + 
					"  Last flood scan length is 1, maximum is 10\r\n" + 
					"  Last flood scan time is 0 msec, maximum is 4 msec\r\n" + 
					"  Neighbor Count is 1, Adjacent neighbor count is 1 \r\n" + 
					"    Adjacent with neighbor 10.100.20.220  (Backup Designated Router)\r\n" + 
					"  Suppress hello for 0 neighbor(s)\r\n" + 
					"FastEthernet1/4 is up, line protocol is up (connected)\r\n" + 
					"  Internet Address 10.100.20.101/30, Area 0 \r\n" + 
					"  Process ID 100, Router ID 10.100.20.211, Network Type BROADCAST, Cost: 1\r\n" + 
					"  Transmit Delay is 1 sec, State BDR, Priority 1\r\n" + 
					"  Designated Router (ID) 10.100.20.217, Interface address 10.100.20.102\r\n" + 
					"  Backup Designated router (ID) 10.100.20.211, Interface address 10.100.20.101\r\n" + 
					"  Timer intervals configured, Hello 10, Dead 40, Wait 40, Retransmit 5\r\n" + 
					"    oob-resync timeout 40\r\n" + 
					"    Hello due in 00:00:08\r\n" + 
					"  Supports Link-local Signaling (LLS)\r\n" + 
					"  Cisco NSF helper support enabled\r\n" + 
					"  IETF NSF helper support enabled\r\n" + 
					"  Index 4/4, flood queue length 0\r\n" + 
					"  Next 0x0(0)/0x0(0)\r\n" + 
					"  Last flood scan length is 1, maximum is 11\r\n" + 
					"  Last flood scan time is 0 msec, maximum is 8 msec\r\n" + 
					"  Neighbor Count is 1, Adjacent neighbor count is 1 \r\n" + 
					"    Adjacent with neighbor 10.100.20.217  (Designated Router)\r\n" + 
					"  Suppress hello for 0 neighbor(s)\r\n" + 
					"FastEthernet1/3 is up, line protocol is up (connected)\r\n" + 
					"  Internet Address 10.100.20.97/30, Area 0 \r\n" + 
					"  Process ID 100, Router ID 10.100.20.211, Network Type BROADCAST, Cost: 10\r\n" + 
					"  Transmit Delay is 1 sec, State BDR, Priority 1\r\n" + 
					"  Designated Router (ID) 10.100.20.213, Interface address 10.100.20.98\r\n" + 
					"  Backup Designated router (ID) 10.100.20.211, Interface address 10.100.20.97\r\n" + 
					"  Timer intervals configured, Hello 10, Dead 100, Wait 100, Retransmit 5\r\n" + 
					"    oob-resync timeout 100\r\n" + 
					"    Hello due in 00:00:08\r\n" + 
					"  Supports Link-local Signaling (LLS)\r\n" + 
					"  Cisco NSF helper support enabled\r\n" + 
					"  IETF NSF helper support enabled\r\n" + 
					"  Index 3/3, flood queue length 0\r\n" + 
					"  Next 0x0(0)/0x0(0)\r\n" + 
					"  Last flood scan length is 1, maximum is 17\r\n" + 
					"  Last flood scan time is 0 msec, maximum is 4 msec\r\n" + 
					"  Neighbor Count is 1, Adjacent neighbor count is 1 \r\n" + 
					"    Adjacent with neighbor 10.100.20.213  (Designated Router)\r\n" + 
					"  Suppress hello for 0 neighbor(s)\r\n" + 
					"FastEthernet1/2 is up, line protocol is up (connected)\r\n" + 
					"  Internet Address 10.100.20.94/30, Area 0 \r\n" + 
					"  Process ID 100, Router ID 10.100.20.211, Network Type BROADCAST, Cost: 1\r\n" + 
					"  Transmit Delay is 1 sec, State BDR, Priority 1\r\n" + 
					"  Designated Router (ID) 10.100.20.210, Interface address 10.100.20.93\r\n" + 
					"  Backup Designated router (ID) 10.100.20.211, Interface address 10.100.20.94\r\n" + 
					"  Timer intervals configured, Hello 10, Dead 100, Wait 100, Retransmit 5\r\n" + 
					"    oob-resync timeout 100\r\n" + 
					"    Hello due in 00:00:08\r\n" + 
					"  Supports Link-local Signaling (LLS)\r\n" + 
					"  Cisco NSF helper support enabled\r\n" + 
					"  IETF NSF helper support enabled\r\n" + 
					"  Index 2/2, flood queue length 0\r\n" + 
					"  Next 0x0(0)/0x0(0)\r\n" + 
					"  Last flood scan length is 1, maximum is 17\r\n" + 
					"  Last flood scan time is 0 msec, maximum is 4 msec\r\n" + 
					"  Neighbor Count is 1, Adjacent neighbor count is 1 \r\n" + 
					"    Adjacent with neighbor 10.100.20.210  (Designated Router)\r\n" + 
					"  Suppress hello for 0 neighbor(s)\r\n" + 
					"FastEthernet1/1 is up, line protocol is up (connected)\r\n" + 
					"  Internet Address 10.100.20.4/29, Area 0 \r\n" + 
					"  Process ID 100, Router ID 10.100.20.211, Network Type BROADCAST, Cost: 1\r\n" + 
					"  Transmit Delay is 1 sec, State DROTHER, Priority 1\r\n" + 
					"  Designated Router (ID) 10.100.32.1, Interface address 10.100.20.1\r\n" + 
					"  Backup Designated router (ID) 10.100.20.213, Interface address 10.100.20.3\r\n" + 
					"  Timer intervals configured, Hello 10, Dead 100, Wait 100, Retransmit 5\r\n" + 
					"    oob-resync timeout 100\r\n" + 
					"    Hello due in 00:00:08\r\n" + 
					"  Supports Link-local Signaling (LLS)\r\n" + 
					"  Cisco NSF helper support enabled\r\n" + 
					"  IETF NSF helper support enabled\r\n" + 
					"  Index 1/1, flood queue length 0\r\n" + 
					"  Next 0x0(0)/0x0(0)\r\n" + 
					"  Last flood scan length is 1, maximum is 17\r\n" + 
					"  Last flood scan time is 0 msec, maximum is 96 msec\r\n" + 
					"  Neighbor Count is 3, Adjacent neighbor count is 2 \r\n" + 
					"    Adjacent with neighbor 10.100.20.213  (Backup Designated Router)\r\n" + 
					"    Adjacent with neighbor 10.100.32.1  (Designated Router)\r\n" + 
					"  Suppress hello for 0 neighbor(s)\r\n" + 
					"6506modular#";
			String eigrp = "show ip eigrp topology\r\n" + 
					"IP-EIGRP Topology Table for AS(1)/ID(10.100.20.213)\r\n" + 
					"\r\n" + 
					"Codes: P - Passive, A - Active, U - Update, Q - Query, R - Reply,\r\n" + 
					"       r - reply Status, s - sia Status \r\n" + 
					"\r\n" + 
					"P 10.100.20.96/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/4\r\n" + 
					"P 10.100.20.72/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/1\r\n" + 
					"P 10.100.20.24/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/2\r\n" + 
					"P 10.100.20.12/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/0\r\n" + 
					"P 10.100.20.0/29, 1 successors, FD is 258560\r\n" + 
					"         via Connected, FastEthernet0/0\r\n" + 
					"P 10.100.16.0/24, 1 successors, FD is 297244416\r\n" + 
					"         via Connected, Tunnel205\r\n" + 
					"P 10.100.20.213/32, 1 successors, FD is 384000\r\n" + 
					"         via Connected, Loopback0\r\n" + 
					"P 10.100.20.200/29, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/5\r\n" + 
					"SEA-7206#show ip eigrp topology\r\n" + 
					"	show ip eigrp topology\r\n" + 
					"IP-EIGRP Topology Table for AS(1)/ID(10.100.20.213)\r\n" + 
					"\r\n" + 
					"Codes: P - Passive, A - Active, U - Update, Q - Query, R - Reply,\r\n" + 
					"       r - reply Status, s - sia Status \r\n" + 
					"\r\n" + 
					"P 10.100.20.96/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/4\r\n" + 
					"P 10.100.20.72/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/1\r\n" + 
					"P 10.100.20.24/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/2\r\n" + 
					"P 10.100.20.12/30, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/0\r\n" + 
					"P 10.100.20.0/29, 1 successors, FD is 258560\r\n" + 
					"         via Connected, FastEthernet0/0\r\n" + 
					"P 10.100.16.0/24, 1 successors, FD is 297244416\r\n" + 
					"         via Connected, Tunnel205\r\n" + 
					"P 10.100.20.213/32, 1 successors, FD is 384000\r\n" + 
					"         via Connected, Loopback0\r\n" + 
					"P 10.100.20.200/29, 1 successors, FD is 281600\r\n" + 
					"         via Connected, Ethernet4/5\r\n" + 
					"SEA-7206#";
			JSONObject backup = new JSONObject();
			backup.put("core:systemName", IRegexUtils.getFirstGrop(
					runningConfig, "^hostname (\\S+)", Pattern.MULTILINE));
			/*JSONObject osInfo = new JSONObject();
			osInfo.put("core:fileName", IRegexUtils.getFirstGrop
					version, "^System image file is \"([^\"]+)", Pattern.MULTILINE));
			osInfo.put("core:make", "Cisco");
			osInfo.put("core:name", IRegexUtils.getFirstGrop
					version, "^(IOS.+?),", Pattern.MULTILINE));
			String ver = IRegexUtils.getFirstGrop
					version, "^(?:Cisco )?IOS.+Version (\\S[^\\s,]+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(ver)) {
				ver = IRegexUtils.getFirstGrop
						version, "^Version\\s+V(\\d+\\.\\d+\\.\\d+)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			osInfo.put("core:version", ver);
			osInfo.put("core:osType", "IOS");
			backup.put("core:osInfo", osInfo);*/

			/*String bios = IRegexUtils.getFirstGrop
					version, "ROM:\\s+System\\s+Bootstrap[,]\\s+Version\\s+([^\\s,]+)",
					Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(bios)) {
				bios = IRegexUtils.getFirstGrop
						version, "ROM:\\s+Bootstrap\\s+program\\s+is\\s+(.*)",
						Pattern.CASE_INSENSITIVE);
			}
			if (IUtils.isNullOrEmpty(bios)) {
				bios = IRegexUtils.getFirstGrop
						version, "BOOTLDR:\\s+\\S+\\s+Boot\\s+Loader.*Version\\s+([^\\s,]+)",
						Pattern.CASE_INSENSITIVE);
			}
			if (IUtils.isNullOrEmpty(bios)) {
				bios = IRegexUtils.getFirstGrop
						version, "^ROM:\\s+TinyROM\\s+version\\s+([^\\s]+)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			if (IUtils.isNullOrEmpty(bios)) {
				bios = IRegexUtils.getFirstGrop
						version, "^ROM:\\s+([^\\s]+)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			backup.put("core:biosVersion", bios);*/
			/*String deviceType = null;
			if (IRegexUtils.isMatch(
					version, "\\b(cat|(WS|ME)-C)\\d{4}|catalyst",
					Pattern.CASE_INSENSITIVE)) {
				deviceType = "Switch";
			} else if (IRegexUtils.isMatch(
					version, "\\bC1200\\b|\\bAIR")) {
				deviceType = "Wireless Access Point";
			} else {
				deviceType = "Router";
			}
			backup.put("core:deviceType", deviceType);
			backup.put("core:contact", IRegexUtils.getFirstGrop(
					runningConfig, "^snmp-server contact (.+)", Pattern.MULTILINE));*/
			/*List<String> restart = IRegexUtils.getMatchingGrops(
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, version,
					"System restarted (?:by \\S+\\s)?at\\s+(\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+(\\S+)\\s+\\S+\\s+(\\S+)\\s+(\\d{1,2})\\s+(\\d{4})");
			if (!IUtils.isNull(restart) && !restart.isEmpty() && restart.size() > 5) {
				long rest = IRegexUtils.getTimeFromEpoch(restart);
				if (rest > 0) {
					backup.put("core:lastReboot", rest);
				}
			} else if (IRegexUtils.isMatch(version, "uptime is\\s+(.*)", Pattern.CASE_INSENSITIVE)) {
				String uptime = IRegexUtils.getFirstGrop(
						version, "uptime is\\s+(.*)", Pattern.CASE_INSENSITIVE);
				long upt = IRegexUtils.getUptimeFromEpoch(uptime);
				if (upt > 0) {
					backup.put("core:lastReboot", upt);
				}
			}*/
			JSONObject file_systems = new JSONObject();
			file_systems.put("flash", "\r\n" + 
					"Directory of flash:/\r\n" + 
					"\r\n" + 
					"    2  -rwx     6072902  Aug 16 2007 10:50:59 -05:00  c3750-ipbasek9-mz.122-25.SEB4.bin\r\n" + 
					"    3  -rwx        3256  May 15 2008 13:11:27 -05:00  vlan.dat\r\n" + 
					"    4  drwx           0  Jul 28 2006 09:48:58 -05:00  crashinfo_ext\r\n" + 
					"    5  -rwx     8183787  Aug 16 2007 10:55:35 -05:00  c3750-advipservicesk9-mz.122-25.SEE4.bin\r\n" + 
					"    7  -rwx        8597  Aug 27 2008 10:35:42 -05:00  config.text\r\n" + 
					"  364  drwx          64  Feb 28 1993 18:01:15 -06:00  crashinfo\r\n" + 
					"    6  -rwx        1960  Aug 27 2008 10:35:42 -05:00  private-config.text\r\n" + 
					"    9  -rwx        1048  Aug 27 2008 10:35:42 -05:00  multiple-fs\r\n" + 
					"\r\n" + 
					"15998976 bytes total (1722368 bytes free)\r\n" + 
					"labcore1#");
			/*List<String> fss = IRegexUtils.getMatchingGrops(Pattern.MULTILINE, fs, "^\\*?\\s+\\d+\\s+\\d+.*:");
			if (!IUtils.isNull(fss)) {
				file_systems = new JSONObject();
				for (String s : fss) {
					System.out.println("s: " + s);
					List<String> f = IRegexUtils.getMatchingGrops(s, "^\\*?\\s+\\d+\\s+\\d+\\s+\\b(\\S+)\\b\\s+[A-Za-z]+\\s+(\\S+):");
					System.out.println("f: " + f);
					if (!IRegexUtils.isMatch(f.get(1), "opaque|nvram", Pattern.CASE_INSENSITIVE)) {
						String key = f.get(2);
						String val = null;
						try {
							val = execute("show " + key);
						} catch (Exception e) {
							// ignore it
						}
						if (!IUtils.isNullOrEmpty(key) && !IUtils.isNullOrEmpty(val)) {
							CiscoIOSParser.addInJson(file_systems, key, val);
						}
					}
				}
			}*/
			/*if (IRegexUtils.isMatch(version, "\\bWS-C\\d{4}|Cisco\\s*76\\d{2}", Pattern.CASE_INSENSITIVE)) {
				// on switches and 7600 routers execute "show module"
				System.out.println("Found");
			} else {
				System.out.println("NOT Found");
			}*//*
			String prcsrBrd = IRegexUtils.getFirstGrop(version,
					"Cisco \\S+\\s+\\((.*)\\) processor\\s+.*\\s+with \\S+ bytes of memory",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(prcsrBrd)) {
				prcsrBrd = IRegexUtils.getFirstGrop(version,
						"Cisco \\S+\\s+\\((.*)\\) processor with \\S+ bytes of memory.",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			int cpuid = -1;
			String desc = null;
			if (!IUtils.isNullOrEmpty(prcsrBrd)) {
				cpuid = 0;
			} else {
				desc = IRegexUtils.getFirstGrop(version,
						"^(\\S+\\s+CPU\\s+at\\s+\\S+,.*)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
				if (IUtils.isNullOrEmpty(desc)) {
					desc = IRegexUtils.getFirstGrop(version,
							"^(\\S+)\\s+CPU\\s+at\\s+\\S+,.*",
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					if (IUtils.isNullOrEmpty(desc)) {
						desc = IRegexUtils.getFirstGrop(version,
								"Cisco Catalyst ([19|28].*\\s+processor) with \\S+ bytes of memory",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
						if (IUtils.isNullOrEmpty(desc)) {
							desc = IRegexUtils.getFirstGrop(version,
									"CPU part number (\\w+)",
									Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
						}
					}
				}
			}
			String sysImg = null;
			String actSlot = null;
			if (!IUtils.isNullOrEmpty(desc)) {
				cpuid = 0;
			} else {
				sysImg = IRegexUtils.getFirstGrop(version,
						"System\\s+image\\s+file\\s+is\\s+\"(\\S+)\"",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
				if (!IUtils.isNullOrEmpty(sysImg)) {
					actSlot = IRegexUtils.getFirstGrop(sysImg,
							"(\\S+):(\\S+)", Pattern.CASE_INSENSITIVE);
					if (IUtils.isNullOrEmpty(actSlot)) {
						actSlot = IRegexUtils.getFirstGrop(version,
								"System\\s+image\\s+file\\s+is\\s+\"\\S+\".*via\\s+([A-Za-z]+)",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					}
				}
			}
			int dualFlash = 0;
			String flashpc0type = null;
			String flashpc0name = null;
			int flashpc0size = -1;
			int flashpc0num = 0;
			String flashpc1type = null;
			String flashpc1name = null;
			int flashpc1size = -1;
			int flashpc1num = 0;
			if (IUtils.isNullOrEmpty(sysImg)) {
				List<String> flash = IRegexUtils.getMatchingGrops(
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, version,
						"(\\d+)K bytes of Flash PCMCIA card at (\\w+) (\\d) \\(");
				if (!IUtils.isNull(flash) && flash.size() >= 4) {
					dualFlash = 1;
					int type = IUtils.parseInt(flash.get(3));
					if (type == 0) {
						flashpc0type = flash.get(2) + type;
						flashpc0name = "pcmciacard";
						flashpc0size = IUtils.parseInt(flash.get(1));
					} else if (type == 1) {
						flashpc1type = flash.get(2) + type;
						flashpc1name = "pcmciacard";
						flashpc1size = IUtils.parseInt(flash.get(1));
						flashpc1num = 1;
					}
				}
			}
			// --pu
			String flashpu0type = null;
			String flashpu0name = null;
			int flashpu0size = -1;
			int flashpu0num = 0;
			String flashpu1type = null;
			String flashpu1name = null;
			int flashpu1size = -1;
			int flashpu1num = 0;
			if (IUtils.isNullOrEmpty(flashpc0name) && IUtils.isNullOrEmpty(flashpc1name)) {
				List<String> flash = IRegexUtils.getMatchingGrops(
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, version,
						"(\\d+)K bytes of processor board PCMCIA (\\w+) flash \\(");
				if (!IUtils.isNull(flash) && flash.size() >= 4) {
					dualFlash = 1;
					String slotnum = flash.get(2);
					if (!IUtils.isNullOrEmpty(slotnum) && slotnum.contains("0")) {
						flashpu0type = IUtils.changeFirstCharCaseOfWords(slotnum, false);
						flashpu0name = "pcmcia";
						flashpu0size = IUtils.parseInt(flash.get(1));
					} else if (!IUtils.isNullOrEmpty(slotnum) && slotnum.contains("1")) {
						flashpu1type = IUtils.changeFirstCharCaseOfWords(slotnum, false);
						flashpu1name = "pcmcia";
						flashpu1size = IUtils.parseInt(flash.get(1));
						flashpu1num = 1;
					}
				}
			}
			String flashp1type = null;
			String flashp1name = null;
			int flashp1size = -1;
			int flashp1num = 0;
			if (IUtils.isNullOrEmpty(flashpu0name) && IUtils.isNullOrEmpty(flashpu1name)) {
				String flashSize = IRegexUtils.getFirstGrop(version,
						"(\\d+)K bytes of processor board System flash partition 1 \\(",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
				if (!IUtils.isNullOrEmpty(flashSize)) {
					dualFlash = 1;
					flashp1type = "flash";
					flashp1name = "partition";
					flashp1size = IUtils.parseInt(flashSize);
					flashp1num = 1;
				}
			}
			String flashp2type = null;
			String flashp2name = null;
			int flashp2size = -1;
			int flashp2num = 0;
			if (IUtils.isNullOrEmpty(flashp1type)) {
				String flashSize = IRegexUtils.getFirstGrop(version,
						"(\\d+)K bytes of processor board System flash partition 2 \\(",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
				if (!IUtils.isNullOrEmpty(flashSize)) {
					dualFlash = 1;
					flashp2type = "flash";
					flashp2name = "partition";
					flashp2size = IUtils.parseInt(flashSize);
					flashp2num = 2;
				}
			}
			String flashpbtype = null;
			String flashpbname = null;
			int flashpbsize = -1;
			if (IUtils.isNullOrEmpty(flashp2type)) {
				String flashSize = IRegexUtils.getFirstGrop(version,
						"(\\d+)K bytes of processor board System flash \\(",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
				if (!IUtils.isNullOrEmpty(flashSize)) {
					dualFlash = 1;
					flashpbtype = "flash";
					flashpbname = "none";
					flashpbsize = IUtils.parseInt(flashSize);
				}
			}
			String ramMemory = null;
			String packetMemory = null;
			List<String> memory = IRegexUtils.getMatchingGrops(
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, version,
					"Cisco\\s+.*with\\s+(\\d+)K\\/(\\d+)K\\s+bytes of .*memory");
			if (IUtils.isNull(memory) || memory.isEmpty()) {
				memory = IRegexUtils.getMatchingGrops(
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, version,
						"processor.*with\\s+(\\d+)K\\/(\\d+)K\\s+bytes of .*memory");
			}
			if (!IUtils.isNull(memory) && memory.size() >= 3) {
				ramMemory = memory.get(1);
				packetMemory = memory.get(2);
				 ------NOT implemented part--------
				 * # Round up the ram size, if residue > 819 KBs (80% of 1 MB).
					my $roundup = $ramMemory % 1024 > $KBS_80PERCENTMB ? 1 : 0;
					my $ramMBs = int( my $volatile = $ramMemory / 1024 ) + $roundup;																												
		
					# Use the ram size value, if it is reasonable (2**n);
					# Otherwise, choose the (ram+packet) sum as the size value.
					$ramMemory +=
					  ( $SIZEPATTERNS_STRING =~ /\s($ramMBs)\s/ )
					  ? 0
					  : $packetMemory;
				 
			} else {
				ramMemory = IRegexUtils.getFirstGrop(version,
						"Cisco\\s+.*with\\s+(\\d+)K\\s+bytes of .*memory",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			String configMemory = null;
			String cMem = IRegexUtils.getFirstGrop(version,
					"(\\d+)K bytes of non-volatile configuration memory",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(cMem)) {
				cMem = IRegexUtils.getFirstGrop(version,
						"(\\d+)K bytes of flash-simulated non-volatile configuration memory.",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			if (IUtils.isNullOrEmpty(cMem)) {
				configMemory = cMem;
			} else {
				configMemory = IRegexUtils.getFirstGrop(version,
						"(\\d+)K bytes of NVRAM",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			String ciscoModel = IRegexUtils.getFirstGrop(version,
					"^IOS\\s+\\(tm\\)\\s+c(16|17|26|29)\\d\\d(XL)?\\s+Software.*",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			String serialNumber = null;
			if (!IUtils.isNullOrEmpty(ciscoModel)) {
				if (ciscoModel.matches("^16$|^17$|^26$")) {
					serialNumber = IRegexUtils.getFirstGrop(version,
							"^Processor\\s+board\\s+ID\\s+(\\w+)\\s+(\\(\\d+\\))",
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
				} else if (ciscoModel.matches("29")) {
					serialNumber = IRegexUtils.getFirstGrop(version,
							"System\\s+serial\\s+number:\\s+(\\S+)",
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					if (IUtils.isNullOrEmpty(serialNumber)) {
						serialNumber = IRegexUtils.getFirstGrop(version,
								"System\\s+serial\\s+number\\s+:\\s+(\\S+)",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					}
				} 
			}
			if (IUtils.isNullOrEmpty(serialNumber)) {
				serialNumber = IRegexUtils.getFirstGrop(version,
						"^Processor\\s+board\\s+ID\\s+([^,]+)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			// only use the SNMP Chassis ID if it is longer than 5 chars
			String sno = IRegexUtils.getFirstGrop(snmp,
					"^Processor\\s+board\\s+ID\\s+([^,]+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (!IUtils.isNullOrEmpty(sno) && sno.length() >= 5) {
				serialNumber = sno;
			}
			JSONObject chassis = new JSONObject();
			JSONObject asset = new JSONObject();
			asset.put("core:assetType", "Chassis");
			JSONObject factInfo = new JSONObject();
			factInfo.put("core:serialNumber", serialNumber);
			factInfo.put("core:make", "Cisco");
			String modelNumber = IRegexUtils.getFirstGrop(version,
					"^(?:Product\\/)?Model number\\s*:\\s*(\\S+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(modelNumber)) {
				modelNumber = IRegexUtils.getFirstGrop(version,
						"^cisco\\s+((?:WS-C|Cat|AS|C|VG)?\\d{3,4}\\S*\\b)\\S*\\b",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
				if (IUtils.isNullOrEmpty(modelNumber)) {
					modelNumber = IRegexUtils.getFirstGrop(version,
							"^Cisco\\s+(\\S+)(?:\\s\\(\\S+\\))?\\s*processor with",
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					if (IUtils.isNullOrEmpty(modelNumber)) {
						modelNumber = IRegexUtils.getFirstGrop(version,
								"^Cisco\\s+(\\S+)\\s.+?Voice\\sLinecard.+?\\sprocessor(?:\\s\\(.+?\\))? with",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
						if (IUtils.isNullOrEmpty(modelNumber)) {
							modelNumber = IRegexUtils.getFirstGrop(version,
									"^cisco\\s+catalyst\\s+(\\d{3,4})\\s",
									Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
						}
					}
				}
			}
			factInfo.put("core:modelNumber", modelNumber);
			asset.put("core:factoryinfo", factInfo);
			chassis.put("core:asset", asset);
			String description = IRegexUtils.getFirstGrop(version,
					"(^Cisco\\s+(?:Internetwork\\s+Operating\\s+System|IOS)\\s+Software.+^Compiled.+?$)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(description)) {
				description = IRegexUtils.getFirstGrop(version,
						"(Cisco Catalyst .* Enterprise Edition Software.+Copyright.+?$)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			JSONObject card = CiscoIOSParser.parseCard(module, modVer, diag);
			chassis.put("cards", card);
			JSONObject cpu = new JSONObject();
			if (cpuid != -1 && !IUtils.isNullOrEmpty(description)) {
				cpu.put("core:description", description);
			}
			if (cpuid != -1 && !IUtils.isNullOrEmpty(prcsrBrd)) {
				cpu.put("cpuType", prcsrBrd);
			}
			chassis.put("cpu", cpu);
			// parse file storage
			JSONArray fsys = CiscoIOSParser.parseFileStorage(fs, file_systems);
			chassis.put("deviceStorage", fsys);
			String mac = IRegexUtils.getFirstGrop(version,
					"Base\\sEthernet\\s(?:MAC |)Address:\\s+([^\\s]+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(mac)) {
				mac = IRegexUtils.getFirstGrop(version,
						"Ethernet\\sAddress:\\s+([^\\s]+)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			if (!IUtils.isNullOrEmpty(mac)) {
				chassis.put("macAddress", mac);
			}
			// created memories array
			JSONArray jMem = new JSONArray();
			if (flashpbsize != -1 && !IUtils.isNullOrEmpty(flashpbtype)) {
				CiscoIOSParser.addFlashMem(jMem, "Flash", flashpbsize, false);
			}
			if (flashpc0size != -1 && !IUtils.isNullOrEmpty(flashpc0type)) {
				CiscoIOSParser.addFlashMem(jMem, "Flash", flashpc0size, false);
			}
			if (flashpc1size != -1 && !IUtils.isNullOrEmpty(flashpc1type)) {
				CiscoIOSParser.addFlashMem(jMem, "Flash", flashpc1size, false);
			}
			if (flashpu0size != -1 && !IUtils.isNullOrEmpty(flashpu0type)) {
				CiscoIOSParser.addFlashMem(jMem, "Flash", flashpu0size, false);
			}
			if (flashpu1size != -1 && !IUtils.isNullOrEmpty(flashpu1type)) {
				CiscoIOSParser.addFlashMem(jMem, "Flash", flashpu1size, false);
			}
			if (flashp1size != -1 && !IUtils.isNullOrEmpty(flashp1type)) {
				CiscoIOSParser.addFlashMem(jMem, "Flash", flashp1size, false);
			}
			if (flashp2size != -1 && !IUtils.isNullOrEmpty(flashp2type)) {
				CiscoIOSParser.addFlashMem(jMem, "Flash", flashp2size, false);
			}
			if (!IUtils.isNullOrEmpty(ramMemory)) {
				CiscoIOSParser.addFlashMem(jMem, "RAM",
						IRegexUtils.extractInt(ramMemory), true);
			}
			if (!IUtils.isNullOrEmpty(packetMemory)) {
				CiscoIOSParser.addFlashMem(jMem, "PacketMemory",
						IRegexUtils.extractInt(packetMemory), true);
			}
			if (!IUtils.isNullOrEmpty(configMemory)) {
				CiscoIOSParser.addFlashMem(jMem, "ConfigurationMemory",
						IRegexUtils.extractInt(configMemory), false);
			}
			chassis.put("memory", jMem);
			JSONArray aPower = CiscoIOSParser.parsePower(power, inventory, version);
			chassis.put("powersupply", aPower);
			backup.put("chassis", chassis);*/
			String start_config = "show startup-config\r\n" + 
					"Using 8597 out of 524288 bytes\r\n" + 
					"!\r\n" + 
					"! Last configuration change at 10:35:39 UTC Wed Aug 27 2008 by security\r\n" + 
					"! NVRAM config last updated at 10:35:41 UTC Wed Aug 27 2008 by security\r\n" + 
					"!\r\n" + 
					"version 12.2\r\n" + 
					"no service pad\r\n" + 
					"service timestamps debug uptime\r\n" + 
					"service timestamps log datetime\r\n" + 
					"service password-encryption\r\n" + 
					"service sequence-numbers\r\n" + 
					"!\r\n" + 
					"hostname labcore1_1000_5000_1310\r\n" + 
					"!\r\n" + 
					"enable secret 5 $1$QJM5$FzpSSkUoBf7WEEMjYKsF5.\r\n" + 
					"!\r\n" + 
					"username mantest password 7 110B0B0003060D0D163C2E\r\n" + 
					"username security password 7 010503035A18040E2355\r\n" + 
					"username testlab privilege 14 password 7 000C1C0406521F\r\n" + 
					"aaa new-model\r\n" + 
					"aaa authentication login default group tacacs+ local\r\n" + 
					"aaa authentication enable default group tacacs+ enable\r\n" + 
					"aaa authorization commands 15 default group tacacs+ local \r\n" + 
					"aaa accounting commands 15 default start-stop group tacacs+\r\n" + 
					"!\r\n" + 
					"aaa session-id common\r\n" + 
					"clock timezone UTC -6\r\n" + 
					"clock summer-time UTC recurring\r\n" + 
					"switch 1 provision ws-c3750g-24t\r\n" + 
					"ip subnet-zero\r\n" + 
					"ip routing\r\n" + 
					"ip domain-name alterpoint.com\r\n" + 
					"ip name-server 10.10.1.9\r\n" + 
					"!\r\n" + 
					"ip ssh version 1\r\n" + 
					"ipv6 unicast-routing\r\n" + 
					"!\r\n" + 
					"!\r\n" + 
					"crypto pki trustpoint TP-self-signed-2487611008\r\n" + 
					" enrollment selfsigned\r\n" + 
					" subject-name cn=IOS-Self-Signed-Certificate-2487611008\r\n" + 
					" revocation-check none\r\n" + 
					" rsakeypair TP-self-signed-2487611008\r\n" + 
					"!\r\n" + 
					"!\r\n" + 
					"crypto ca certificate chain TP-self-signed-2487611008\r\n" + 
					" certificate self-signed 01 nvram:\r\n" +
					"labcore1#";
			/*JSONObject config = CiscoIOSParser.createConfig(runningConfig, start_config);
			backup.put("core:configRepository", config);
			JSONObject accPorts = CiscoIOSParser.parseAccessPorts(runningConfig);
			backup.put("accessPorts", accPorts);
			String access_lists = "show access-lists\r\n" + 
					"Standard IP access list 10\r\n" + 
					"    10 deny   169.254.0.0, wildcard bits 0.0.255.255 log\r\n" + 
					"    20 permit any (3933 matches)\r\n" + 
					"Standard IP access list 15\r\n" + 
					"    10 permit 10.10.1.89\r\n" + 
					"    20 permit 10.100.32.0, wildcard bits 0.0.0.255\r\n" + 
					"    30 permit 192.168.0.0, wildcard bits 0.0.255.255\r\n" + 
					"Standard IP access list 40\r\n" + 
					"    10 permit 10.100.32.0, wildcard bits 0.0.0.255\r\n" + 
					"Extended IP access list 110\r\n" + 
					"    10 deny ip any host 192.168.2.254 log\r\n" + 
					"    20 deny ip any host 192.168.2.253 log\r\n" + 
					"    30 permit ip 10.100.19.0 0.0.0.255 any\r\n" + 
					"    40 permit ip 10.100.32.0 0.0.1.255 any (456587 matches)\r\n" + 
					"    50 permit ip 10.100.0.0 0.0.31.255 10.0.0.0 0.255.255.255 (95664 matches)\r\n" + 
					"    60 permit ip 10.100.0.0 0.0.31.255 192.168.0.0 0.0.255.255 (1681 matches)\r\n" + 
					"    70 permit ip 10.20.0.0 0.0.255.255 any (929 matches)\r\n" + 
					"    80 permit ip host 10.100.3.16 any\r\n" + 
					"    90 permit ip 10.100.7.0 0.0.0.255 any (318 matches)\r\n" + 
					"labcore1#";
			JSONObject filters = CiscoIOSParser.parseFilters(access_lists );
			backup.put("filterLists", filters);*/
			//JSONObject ifs = CiscoIOSParser.parseInterfaces(runningConfig, interfaces, ipInterface);
			//<json>.put("interfaces", ifs);
			//backup.put("interfaces", ifs);
			//JSONArray lcac = CiscoIOSParser.parseLocalAccounts(runningConfig);
			//<json>.put("localAccounts", lcac);
			//backup.put("localAccounts", lcac);
			//JSONObject routing = CiscoIOSParser.parseRouting(runningConfig, ipProto);
			//<json>.put"cisco:routing", routing);
			//backup.put("cisco:routing", routing);
			//JSONObject jsnmp = CiscoIOSParser.parseSnmp(runningConfig, snmp);
			//<json>.put"snmp", jsnmp);
			//backup.put("snmp", jsnmp);
			//JSONArray span = CiscoIOSParser.parseSTP(spanTree);
			//<json>.put("spanningTree", span);
			//backup.put("spanningTree", span);
			//JSONArray srout = CiscoIOSParser.parseStaticRoutes(runningConfig);
			//<json>.put("staticRoutes", srout);
			//backup.put("staticRoutes", srout);
			String vlan = "show vlan\r\n" + 
					"\r\n" + 
					"VLAN Name                             Status    Ports\r\n" + 
					"---- -------------------------------- --------- -------------------------------\r\n" + 
					"1    default                          active    Fa1/9, Fa1/22\r\n" + 
					"403  radical                          active    Fa1/18\r\n" + 
					"1002 fddi-default                     act/unsup \r\n" + 
					"1003 token-ring-default               act/unsup \r\n" + 
					"1004 fddinet-default                  act/unsup \r\n" + 
					"1005 trnet-default                    act/unsup \r\n" + 
					"\r\n" + 
					"VLAN Type  SAID       MTU   Parent RingNo BridgeNo Stp  BrdgMode Trans1 Trans2\r\n" + 
					"---- ----- ---------- ----- ------ ------ -------- ---- -------- ------ ------\r\n" + 
					"1    enet  100001     1500  -      -      -        -    -        0      0   \r\n" + 
					"403  enet  100403     1500  -      -      -        -    -        0      0   \r\n" + 
					"1002 fddi  101002     1500  -      -      -        -    -        0      0   \r\n" + 
					"1003 tr    101003     1500  -      -      -        -    -        0      0   \r\n" + 
					"1004 fdnet 101004     1500  -      -      -        ieee -        0      0   \r\n" + 
					"1005 trnet 101005     1500  -      -      -        ibm  -        0      0   \r\n" + 
					"\r\n" + 
					"Remote SPAN VLANs\r\n" + 
					"------------------------------------------------------------------------------\r\n" + 
					"\r\n" + 
					"\r\n" + 
					"Primary Secondary Type              Ports\r\n" + 
					"------- --------- ----------------- ------------------------------------------\r\n" + 
					"\r\n" + 
					"6506modular#";
			//JSONArray arrVlans = CiscoIOSParser.parseVlans(vlan);
			//<json>.put("vlans", arrVlans);
			//backup.put("vlans", arrVlans);
			JSONObject vtpstatus = CiscoIOSParser.parseVtp(vtp);
			//<json>.put("cisco:vlanTrunking", vtpstatus);
			backup.put("cisco:vlanTrunking", vtpstatus);
			
			System.out.println("Res: " + backup);
			IZTUtils.maskToBit("255.255.255.0");
			/*List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(
					version, "^(C.*)$", Pattern.MULTILINE);
			System.out.println("global match: \n" + lst);
			int i = 0;
			for (List<String> l : lst) {
				System.out.println(++i + ": " + l);
			}*/
			/*backup.put("core:", IRegexUtils.getFirstGrop
					src, "", Pattern.MULTILINE));
			jsn.put("", "");*/

			/*TelnetUtils telnet = new TelnetUtils("127.0.0.1", "testlab",
					"hobbit");
			System.out.println("Got Connection...");
			TelnetUtils.prompt = "#";
			System.out.println("Res: " + telnet.sendCommand("terminal length 0"));
			System.out.println("Res: " + telnet.sendCommand("show version"));
			System.out.println("Res: " + telnet.sendCommand("show snmp"));
			System.out.println("Res: " + telnet.sendCommand("show running-config"));
			System.out.println("Res: " + telnet.sendCommand("show file systems"));
			System.out.println("Res: " + telnet.sendCommand("show module"));
			System.out.println("Res: " + telnet.sendCommand("show power"));
			System.out.println("Res: " + telnet.sendCommand("show inventory"));
			System.out.println("Res: " + telnet.sendCommand("show mod ver"));
			System.out.println("Res: " + telnet.sendCommand("show diagbus"));
			System.out.println("Res: " + telnet.sendCommand("show diag"));
			System.out.println("Res: " + telnet.sendCommand("show access-lists"));
			System.out.println("Res: " + telnet.sendCommand("show interfaces"));
			System.out.println("Res: " + telnet.sendCommand("show ip ospf interface"));
			System.out.println("Res: " + telnet.sendCommand("show ip ospf"));
			System.out.println("Res: " + telnet.sendCommand("show ip protocols"));
			System.out.println("Res: " + telnet.sendCommand("show ip eigrp topology"));
			System.out.println("Res: " + telnet.sendCommand("show spanning-tree"));
			System.out.println("Res: " + telnet.sendCommand("show vtp status"));
			telnet.disconnect();*/
			
			//execute("show ip ospf interface,show ip ospf");
			//execute("show ip eigrp topology");
			//execute("show vtp status");
			//execute("show vlan");
			
			System.out.println("DONE");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String execute(String cmd) {
		String res = null;
		TelnetUtils telnet = new TelnetUtils("127.1.0.9", "testlab",
				"hobbit");
		System.out.println("Got Connection...");
		TelnetUtils.prompt = "#";
		if (cmd.contains(",")) {
			StringBuilder sb = new StringBuilder();
			for (String c : cmd.split(",")) {
				sb.append("\r\n\r\n--------\r\n\r\n" + c + "\r\n\r\n--------\r\n\r\n");
				sb.append(telnet.sendCommand(c));
			}
			res = sb.toString();
		} else {
			res = telnet.sendCommand(cmd);
		}
		telnet.disconnect();
		System.out.println("Response...");
		/*for (int i = 1; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 1; k < 10; k++) {
					String ip = "127." + i + "." + j + "." + k;
					TelnetUtils telnet = null;
					try {
						telnet = new TelnetUtils(ip, "testlab",
								"hobbit");
						System.out.println("Connected to " + ip);
						TelnetUtils.prompt = "#";
						cmd = "show ip eigrp topology";
						res = telnet.sendCommand(cmd);
						System.out.println(cmd + "\n\t" + res);
					} catch (Exception ex) {
						System.out.println("ERROR: " + ex.getMessage());
					} finally {
						try {
							telnet.disconnect();
						} catch (Exception ex) {}
					}
				}
			}
		}*/
		return res;
	}

}
