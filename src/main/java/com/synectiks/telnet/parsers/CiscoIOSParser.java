/**
 * 
 */
package com.synectiks.telnet.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.commons.utils.IUtils;
import com.synectiks.telnet.utils.IRegexUtils;
import com.synectiks.telnet.utils.IZTUtils;

/**
 * @author Rajesh
 */
public class CiscoIOSParser {

	private static final Logger logger = LoggerFactory.getLogger(CiscoIOSParser.class);

	private CiscoIOSParser() {
	}

	public static JSONObject parseSystem(String version, String runConfig, String snmp) {
		if (IUtils.isNullOrEmpty(runConfig))
			return new JSONObject();
		JSONObject backup = new JSONObject();
		try {
			backup.put("core:systemName", IRegexUtils.getFirstGrop(
					runConfig, "^hostname (\\S+)", Pattern.MULTILINE));
			backup.put("core:osInfo", parseOsInfo(version));
			backup.put("core:biosVersion", parseBiosVersion(version));
			backup.put("core:deviceType", parseDeviceType(version));
			backup.put("core:contact", IRegexUtils.getFirstGrop(
					runConfig, "^snmp-server contact (.+)", Pattern.MULTILINE));
			List<String> restart = IRegexUtils.getMatchingGrops(
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
			}
		} catch (JSONException e) {
			// ignore it
		}
		return backup;
	}

	/**
	 * Method to extract os-info from version response.
	 * @param version
	 * @return
	 */
	public static JSONObject parseOsInfo(String version) {
		if (IUtils.isNullOrEmpty(version)) return null;
		JSONObject osInfo = new JSONObject();
		try {
			osInfo.put("core:fileName", IRegexUtils.getFirstGrop(
					version, "^System image file is \"([^\"]+)", Pattern.MULTILINE));
			osInfo.put("core:make", "Cisco");
			osInfo.put("core:name", IRegexUtils.getFirstGrop(
					version, "^(IOS.+?),", Pattern.MULTILINE));
			String ver = IRegexUtils.getFirstGrop(
					version, "^(?:Cisco )?IOS.+Version (\\S[^\\s,]+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(ver)) {
				ver = IRegexUtils.getFirstGrop(
						version, "^Version\\s+V(\\d+\\.\\d+\\.\\d+)",
						Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			}
			osInfo.put("core:version", ver);
			osInfo.put("core:osType", "IOS");
		} catch (JSONException e) {
			// ignore it
		}
		return osInfo;
	}

	/**
	 * Method to parse bios version from version string.
	 * @param version
	 * @return
	 */
	public static String parseBiosVersion(String version) {
		if (IUtils.isNullOrEmpty(version)) return null;
		String bios = IRegexUtils.getFirstGrop(
				version, "ROM:\\s+System\\s+Bootstrap[,]\\s+Version\\s+([^\\s,]+)",
				Pattern.CASE_INSENSITIVE);
		if (IUtils.isNullOrEmpty(bios)) {
			bios = IRegexUtils.getFirstGrop(
					version, "ROM:\\s+Bootstrap\\s+program\\s+is\\s+(.*)",
					Pattern.CASE_INSENSITIVE);
		}
		if (IUtils.isNullOrEmpty(bios)) {
			bios = IRegexUtils.getFirstGrop(
					version, "BOOTLDR:\\s+\\S+\\s+Boot\\s+Loader.*Version\\s+([^\\s,]+)",
					Pattern.CASE_INSENSITIVE);
		}
		if (IUtils.isNullOrEmpty(bios)) {
			bios = IRegexUtils.getFirstGrop(
					version, "^ROM:\\s+TinyROM\\s+version\\s+([^\\s]+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		}
		if (IUtils.isNullOrEmpty(bios)) {
			bios = IRegexUtils.getFirstGrop(
					version, "^ROM:\\s+([^\\s]+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		}
		return bios;
	}

	/**
	 * Method to calculate device type.
	 * @param version
	 * @return
	 */
	public static String parseDeviceType(String version) {
		String deviceType = null;
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
		return deviceType;
	}

	/**
	 * Method to parse chassis information from results.
	 * @param version
	 * @param runConfig
	 * @param snmp
	 * @param file_systems
	 * @param diag 
	 * @param modVer 
	 * @param inventory 
	 * @param power 
	 * @param module 
	 */
	public static JSONObject parseChassis(String version, String runConfig, String snmp,
			String fs, JSONObject file_systems, String module, String power, String inventory,
			String modVer, String diag) {

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
		@SuppressWarnings("unused")
		int dualFlash = 0;
		String flashpc0type = null;
		String flashpc0name = null;
		int flashpc0size = -1;
		@SuppressWarnings("unused")
		int flashpc0num = 0;
		String flashpc1type = null;
		String flashpc1name = null;
		int flashpc1size = -1;
		@SuppressWarnings("unused")
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
		@SuppressWarnings("unused")
		int flashpu0num = 0;
		String flashpu1type = null;
		String flashpu1name = null;
		int flashpu1size = -1;
		@SuppressWarnings("unused")
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
		@SuppressWarnings("unused")
		String flashp1name = null;
		int flashp1size = -1;
		@SuppressWarnings("unused")
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
		@SuppressWarnings("unused")
		String flashp2name = null;
		int flashp2size = -1;
		@SuppressWarnings("unused")
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
		@SuppressWarnings("unused")
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
			/* ------NOT implemented part--------
			 * # Round up the ram size, if residue > 819 KBs (80% of 1 MB).
				my $roundup = $ramMemory % 1024 > $KBS_80PERCENTMB ? 1 : 0;
				my $ramMBs = int( my $volatile = $ramMemory / 1024 ) + $roundup;																												
	
				# Use the ram size value, if it is reasonable (2**n);
				# Otherwise, choose the (ram+packet) sum as the size value.
				$ramMemory +=
				  ( $SIZEPATTERNS_STRING =~ /\s($ramMBs)\s/ )
				  ? 0
				  : $packetMemory;
			 */
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
		IZTUtils.addInJson(asset, "core:assetType", "Chassis");
		
		JSONObject factInfo = new JSONObject();
		IZTUtils.addInJson(factInfo, "core:serialNumber", serialNumber);
		IZTUtils.addInJson(factInfo, "core:make", "Cisco");
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
		IZTUtils.addInJson(factInfo, "core:modelNumber", modelNumber);
		IZTUtils.addInJson(asset, "core:factoryinfo", factInfo);
		IZTUtils.addInJson(chassis, "core:asset", asset);
		String description = IRegexUtils.getFirstGrop(version,
				"(^Cisco\\s+(?:Internetwork\\s+Operating\\s+System|IOS)\\s+Software.+^Compiled.+?$)",
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		if (IUtils.isNullOrEmpty(description)) {
			description = IRegexUtils.getFirstGrop(version,
					"(Cisco Catalyst .* Enterprise Edition Software.+Copyright.+?$)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		}
		JSONObject card = CiscoIOSParser.parseCard(module, modVer, diag);
		IZTUtils.addInJson(chassis, "cards", card);
		JSONObject cpu = new JSONObject();
		if (cpuid != -1 && !IUtils.isNullOrEmpty(description)) {
			IZTUtils.addInJson(cpu, "core:description", description);
		}
		if (cpuid != -1 && !IUtils.isNullOrEmpty(prcsrBrd)) {
			IZTUtils.addInJson(cpu, "cpuType", prcsrBrd);
		}
		IZTUtils.addInJson(chassis, "cpu", cpu);
		// parse file storage
		JSONArray fsys = CiscoIOSParser.parseFileStorage(fs, file_systems);
		IZTUtils.addInJson(chassis, "deviceStorage", fsys);
		String mac = IRegexUtils.getFirstGrop(version,
				"Base\\sEthernet\\s(?:MAC |)Address:\\s+([^\\s]+)",
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		if (IUtils.isNullOrEmpty(mac)) {
			mac = IRegexUtils.getFirstGrop(version,
					"Ethernet\\sAddress:\\s+([^\\s]+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		}
		if (!IUtils.isNullOrEmpty(mac)) {
			IZTUtils.addInJson(chassis, "macAddress", mac);
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
		IZTUtils.addInJson(chassis, "memory", jMem);
		JSONArray aPower = CiscoIOSParser.parsePower(power, inventory, version);
		IZTUtils.addInJson(chassis, "powersupply", aPower);
		return chassis;
	}

	/**
	 * Method to parse card details.
	 * @param module
	 * @param modVer
	 * @param diag
	 * @return
	 */
	public static JSONObject parseCard(String module, String modVer, String diag) {
		JSONObject cards = new JSONObject();
		if (!IUtils.isNullOrEmpty(module)) {
			List<List<String>> matches = IRegexUtils.getGlobalMultiMatchGrops(module,
					"^\\s*(\\d+)\\s+(\\d+)\\s+\\b(.+?)\\s+((?:WS|7600)-\\S+)\\s+(\\S+)\\s*$",
					Pattern.MULTILINE);
			if (!IUtils.isNull(matches) && matches.size() > 0) {
				JSONArray arr = new JSONArray();
				for (List<String> match : matches) {
					if (!IUtils.isNull(match) && match.size() >= 5) {
						JSONObject card = new JSONObject();
						String num = match.get(1);
						IZTUtils.addInJson(card, "slotNumber", num);
						IZTUtils.addInJson(card, "portCount", match.get(2));
						IZTUtils.addInJson(card, "core:description", match.get(3));
						JSONObject asset = new JSONObject();
						IZTUtils.addInJson(asset, "core:assetType", "Card");
						JSONObject factInfo = new JSONObject();
						IZTUtils.addInJson(factInfo, "core:make", "Cisco");
						IZTUtils.addInJson(factInfo, "core:modelNumber", "Unknown");
						IZTUtils.addInJson(factInfo, "core:partNumber", match.get(4));
						IZTUtils.addInJson(factInfo, "core:serialNumber", match.get(5));
						String status = IRegexUtils.getFirstGrop(module,
								"^\\s*" + num + "\\s+[a-f0-9.]{14}\\s+to\\s+[a-f0-9.]{14}\\s+(?:[\\w.()]+)\\s+(?:[\\w.()]+)\\s+(?:\\S+)\\s+(\\S+)",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
						IZTUtils.addInJson(card, "status", status);
						// now get the HW, FW and SW versions from 'show mod ver' output
						String ver = IRegexUtils.getFirstGrop(module,
								"^\\s+" + num + "(.+?)(?:^\\s+\\d+|^\\S+)",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE & Pattern.DOTALL);
						if (!IUtils.isNullOrEmpty(ver)) {
							if (ver.contains("Hw")) {
								IZTUtils.addInJson(factInfo, "core:hardwareVersion", ver.trim());
							} else  if (ver.contains("Fw")) {
								IZTUtils.addInJson(factInfo, "core:firmwareVersion", ver.trim());
							} else  if (ver.contains("Sw")) {
								IZTUtils.addInJson(card, "softwareVersion", ver.trim());
							}
						}
						String blob = IRegexUtils.getFirstGrop(module,
								"^Mod\\s+Sub-Mod(.+?)^\\s*$",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE & Pattern.DOTALL);
						if (!IUtils.isNullOrEmpty(blob)) {
							List<List<String>> blobs = IRegexUtils.getGlobalMultiMatchGrops(blob,
									"^\\s*" + num + "(?:\\/\\d+)?\\s+\\b(.+?)\\s+((?:WS|SPA)-\\S+)\\s+(\\S+)\\s+([\\d.]+)\\s+(\\S+)",
									Pattern.MULTILINE);
							if (!IUtils.isNull(blobs) && blobs.size() > 0) {
								JSONArray dcs = new JSONArray();
								for (List<String> blb : blobs) {
									if (!IUtils.isNull(blb) && blb.size() >= 5) {
										JSONObject dc = new JSONObject();
										IZTUtils.addInJson(dc, "core:description", blb.get(1));
										JSONObject dAsset = new JSONObject();
										IZTUtils.addInJson(dAsset, "core:assetType", "Card");
										JSONObject dFactInfo = new JSONObject();
										IZTUtils.addInJson(dFactInfo, "core:make", "Cisco");
										IZTUtils.addInJson(dFactInfo, "core:modelNumber", "Unknown");
										IZTUtils.addInJson(dFactInfo, "core:partNumber", blb.get(2));
										IZTUtils.addInJson(dFactInfo, "core:serialNumber", blb.get(3));
										IZTUtils.addInJson(dFactInfo, "core:hardwareVersion", blb.get(4));
										IZTUtils.addInJson(dc, "status", blb.get(5));
										IZTUtils.addInJson(dAsset, "core:factoryinfo", dFactInfo);
										IZTUtils.addInJson(dc, "core:asset", dAsset);
										dcs.put(dc);
									}
								}
								IZTUtils.addInJson(card, "daughterCard", dcs);
							}
						}
						//addInJson(, "", );
						arr.put(card);
					}
				}
				IZTUtils.addInJson(cards, "cards", arr);
			}
		} /*else {*/
		if (!IUtils.isNullOrEmpty(diag)) {
			int slotnum   = 0;
			JSONObject cardref = null;
			String desc = null;
			String hwVer = null;
			String snum = null;
			String pnum = null;
			String frupnum = null;
			int dslotnum = -1;
			String cardtype = null;
			@SuppressWarnings("unused")
			String type = null;
			String mslotType = null;
			int mslotnum = -1;
			@SuppressWarnings("unused")
			String mslotname = null;
			String dslotname = null;
			int nodesc = 0;
			int nodescencryptaim = 0;
			int nodescaimmodule = 0;
			int nodescaimcarrier = 0;
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new StringReader(diag));
				String line = null;
				while (!IUtils.isNull(line = reader.readLine())) {
					if (IRegexUtils.isMatch(line, "^Slot\\s+(\\d+):")) {
						slotnum = IRegexUtils.extractInt(line, "^Slot\\s+(\\d+):");
						mslotnum = slotnum;
						mslotType = "physical";
						cardtype = "mother";
					} else if (IRegexUtils.isMatch(line, "^AIM\\s+slot\\s+(\\d+):\\s+([A-Za-z-\\/ ]+)")) {
						List<String> val = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line, "^AIM\\s+slot\\s+(\\d+):\\s+([A-Za-z-\\/ ]+)");
						if (!IUtils.isNull(val) && val.size() >= 3) {
							slotnum = IRegexUtils.extractInt(val.get(1)) + 100;
							mslotnum = IRegexUtils.extractInt(val.get(1));
							desc = val.get(2);
							mslotType = "physical";
							mslotname = "AIM";
							cardtype = "mother";
						}
					} else if (IRegexUtils.isMatch(line, "Encryption\\s+AIM\\s+(\\d+):")) {
						int val = IRegexUtils.extractInt(line, "Encryption\\s+AIM\\s+(\\d+):");
						slotnum = val + 300;
						mslotnum = val;
						mslotType = "physical";
						mslotname = "Encryption AIM";
						nodescencryptaim = 1;
						cardtype = "mother";
					} else if (IRegexUtils.isMatch(line, "Compression\\s+AIM\\s+(\\d+):")) {
						int val = IRegexUtils.extractInt(line, "Compression\\s+AIM\\s+(\\d+):");
						slotnum = val + 500;
						mslotnum = val;
						mslotType = "physical";
						mslotname = "Compression AIM";
						cardtype = "mother";
					} else if (IRegexUtils.isMatch(line, "ATM\\s+AIM\\s*[: ]\\s*(\\d+)")) {
						int val = IRegexUtils.extractInt(line, "ATM\\s+AIM\\s*[: ]\\s*(\\d+)");
						slotnum = val + 900;
						mslotnum = val;
						mslotType = "physical";
						mslotname = "ATM AIM";
						cardtype = "mother";
					} else if (IRegexUtils.isMatch(line, "AIM\\s+Module\\s+in\\s+slot[: ]\\s*(\\d+)")) {
						int val = IRegexUtils.extractInt(line, "AIM\\s+Module\\s+in\\s+slot[: ]\\s*(\\d+)");
						slotnum = val + 800;
						mslotnum = val;
						mslotType = "physical";
						mslotname = "AIM Module";
						nodescaimmodule  = 1;
						cardtype = "mother";
					} else if (IRegexUtils.isMatch(line, "^Slot\\s+(\\d+)\\s+\\(virtual\\):")) {
						int val = IRegexUtils.extractInt(line, "^Slot\\s+(\\d+)\\s+\\(virtual\\):");
						slotnum = val + 200;
						mslotnum = val;
						mslotType = "virtual";
						mslotname = "(virtual)";
						cardtype = "mother";
					} else if (IRegexUtils.isMatch(line, "^Slot\\s+(\\d+):Logical_index\\s+(\\d+)")) {
						List<String> val = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line, "^Slot\\s+(\\d+):Logical_index\\s+(\\d+)");
						if (!IUtils.isNull(val) && val.size() >= 3) {
							slotnum = IRegexUtils.extractInt(val.get(2)) + 400;
							mslotnum = IRegexUtils.extractInt(val.get(1));
							mslotType = "logical";
							mslotname = "Logical_index";
							cardtype = "mother";
						}
					} else if (IRegexUtils.isMatch(line, "([A-Z\\/]*IC) Slot\\s+(\\d+):")) {
						List<String> val = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line, "([A-Z\\/]*IC) Slot\\s+(\\d+):");
						if (!IUtils.isNullOrEmpty(cardtype)) {
							cardref = setCardRef(cardref, slotnum, dslotnum, desc,
									hwVer, snum, pnum, frupnum, dslotname, mslotnum, mslotType);
						}
						if (!IUtils.isNull(val) && val.size() >= 3) {
							dslotnum = IRegexUtils.extractInt(val.get(2));
							dslotname = val.get(1);
							cardtype = "daughter";
						}
					} else if (IRegexUtils.isMatch(line, "AIM\\s+on\\s+Carrier\\s+Card\\s+(\\d+):")) {
						int val = IRegexUtils.extractInt(line, "AIM\\s+on\\s+Carrier\\s+Card\\s+(\\d+):");
						if (!IUtils.isNullOrEmpty(cardtype)) {
							cardref = setCardRef(cardref, slotnum, dslotnum, desc,
									hwVer, snum, pnum, frupnum, dslotname, mslotnum, mslotType);
						}
						dslotnum = val;
						desc = "AIM on Carrier Card " + val;
						dslotname = "AIM on Carrier Card";
						nodescaimcarrier = 1;
						cardtype = "daughter";
					} else if (IRegexUtils.isMatch(line, "(PVDM) Slot\\s+(\\d+):")) {
						List<String> val = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line, "(PVDM) Slot\\s+(\\d+):");
						if (!IUtils.isNullOrEmpty(cardtype)) {
							cardref = setCardRef(cardref, slotnum, dslotnum, desc,
									hwVer, snum, pnum, frupnum, dslotname, mslotnum, mslotType);
						}
						if (!IUtils.isNull(val) && val.size() >= 3) {
							dslotnum = IRegexUtils.extractInt(val.get(2)) + 100;
							dslotname = val.get(1);
							nodesc = 1;
							cardtype = "daughter";
						}
					} else if (IRegexUtils.isMatch(line, "(Packet Voice DSP Module) Slot\\s+(\\d+):")) {
						List<String> val = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line, "(Packet Voice DSP Module) Slot\\s+(\\d+):");
						if (!IUtils.isNullOrEmpty(cardtype)) {
							cardref = setCardRef(cardref, slotnum, dslotnum, desc,
									hwVer, snum, pnum, frupnum, dslotname, mslotnum, mslotType);
						}
						if (!IUtils.isNull(val) && val.size() >= 3) {
							dslotnum = IRegexUtils.extractInt(val.get(2)) + 100;
							dslotname = val.get(1);
							nodesc = 1;
							cardtype = "daughter";
						}
					} else if (IRegexUtils.isMatch(line, "(PA\\s+Bay)\\s+(\\d+)\\s+Information:")) {
						List<String> val = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line, "(PA\\s+Bay)\\s+(\\d+)\\s+Information:");
						if (!IUtils.isNull(val) && val.size() >= 3) {
							dslotnum = IRegexUtils.extractInt(val.get(2));
							dslotname = val.get(1);
							cardtype = "daughter";
						}
					} else if (IRegexUtils.isMatch(line, "(PA)\\s+(\\d+)\\s+Information:")) {
						List<String> val = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line, "(PA)\\s+(\\d+)\\s+Information:");
						if (!IUtils.isNull(val) && val.size() >= 3) {
							dslotnum = IRegexUtils.extractInt(val.get(2));
							dslotname = val.get(1);
							cardtype = "daughter";
						}
					} else if (!IUtils.isNullOrEmpty(cardtype) && IUtils.isNullOrEmpty(desc)) {
						String val = IRegexUtils.getFirstGrop(line,
								"\\s*(.*\\s+Port\\s+adapter,\\s+\\S+\\s+\\S+)", Pattern.CASE_INSENSITIVE);
						if (IUtils.isNullOrEmpty(val)) {
							val = IRegexUtils.getFirstGrop(line,
									"\\s*(.*\\s+[a-z]+.\\s+Port\\s+adapter)", Pattern.CASE_INSENSITIVE);
							if (IUtils.isNullOrEmpty(val)) {
								val = IRegexUtils.getFirstGrop(line,
										"\\s*(.*daughter\\s+card)", Pattern.CASE_INSENSITIVE);
							}
						}
						if (!IUtils.isNullOrEmpty(val)) {
							desc = val;
						}
					} else if (!IUtils.isNullOrEmpty(cardtype)) {
						List<String> val = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
								line, "\\s+([^,]+),\\s+HW\\s+rev\\s+([^,]+),\\s+board\\s+revision\\s+(\\S+)");
						if (!IUtils.isNull(val) && val.size() >= 3) {
							desc = val.get(1);
							hwVer = val.get(2);
						} else {
							val = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									line, "Hardware revision\\s([^ ]+)\\s+Board revision\\s+([^ ]+)");
							if (!IUtils.isNull(val) && val.size() >= 2) {
								hwVer = val.get(1);
							}
						}
						if (IUtils.isNull(val) || val.isEmpty()) {
							val = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									line, "\\s+(Hardware|(.*),\\s+HW)\\s+(Revision|rev)\\s+(\\d+.\\d+)[,\\s+]\\s+Board\\s+Revision\\s+(\\S+)");
							if (!IUtils.isNull(val) && val.size() >= 5) {
								hwVer = val.get(4);
							} else {
								val = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
										line, "((HW)\\s+(rev))\\s+(\\S+),\\s+Board\\s+revision");
								if (!IUtils.isNull(val) && val.size() >= 5) {
									hwVer = val.get(4);
								}
							}
						}
						if (IUtils.isNullOrEmpty(hwVer) &&
								IRegexUtils.isMatch(line, "\\s+Hardware\\s+Revision\\s+:\\s+(\\d+.\\d+)")) {
							hwVer = IRegexUtils.getFirstGrop(line,
									"\\s+Hardware\\s+Revision\\s+:\\s+(\\d+.\\d+)", Pattern.CASE_INSENSITIVE);
						}
					} else if (IUtils.isNullOrEmpty(desc) && nodesc == 0 && nodescencryptaim == 0 &&
							nodescaimmodule == 0 && nodescaimcarrier == 0 &&
							IRegexUtils.isMatch(line, "\\s+(.*)")) {
						// description -- 2nd line after "slot" as default case
						desc = IRegexUtils.getFirstGrop(line, "\\\\s+(.*)", Pattern.CASE_INSENSITIVE);
					} else if (IRegexUtils.isMatch(line, "\\s+Serial\\s+Number[:\\s+]\\s+(\\S+|\\d+)\\s+Part\\s+Number[:\\s+]\\s+(\\S+|\\d+).*")) {
						List<String> val = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
								line, "\\s+Serial\\s+Number[:\\s+]\\s+(\\S+|\\d+)\\s+Part\\s+Number[:\\s+]\\s+(\\S+|\\d+).*");
						if (!IUtils.isNull(val) && val.size() >= 3) {
							snum = val.get(1);
							pnum = val.get(2);
							if (!IUtils.isNullOrEmpty(cardtype) && "daughter".equals(cardtype) &&
									!IUtils.isNullOrEmpty(dslotname) && dslotname.contains("PA")) {
								cardref = setCardRef(cardref, slotnum, dslotnum, desc,
										hwVer, snum, pnum, frupnum, dslotname, mslotnum, mslotType);
							}
						}
					} else if (!IUtils.isNullOrEmpty(cardtype)) {
						if (IUtils.isNullOrEmpty(snum) && IRegexUtils.isMatch(
								line, "\\s+\\S+\\s+Serial\\s+Number\\s+:\\s+(\\w+)")) {
							snum = IRegexUtils.getFirstGrop(line,
									"\\s+\\S+\\s+Serial\\s+Number\\s+:\\s+(\\w+)");
						} else if (IUtils.isNullOrEmpty(pnum) && IRegexUtils.isMatch(
								line, "\\s+Part\\s+Number\\s+:\\s+(\\S+)")) {
							pnum = IRegexUtils.getFirstGrop(line,
									"\\s+Part\\s+Number\\s+:\\s+(\\S+)");
						} else if (IUtils.isNullOrEmpty(frupnum) && IRegexUtils.isMatch(
								line, "FRU Part Number[: ]\\s*([^ ]+)")) {
							frupnum = IRegexUtils.getFirstGrop(line,
									"FRU Part Number[: ]\\s*([^ ]+)");
						} else if (IUtils.isNullOrEmpty(frupnum) && IRegexUtils.isMatch(
								line, "Product \\(FRU\\) Number\\s+:\\s+([^ ]+)")) {
							frupnum = IRegexUtils.getFirstGrop(line,
									"Product \\(FRU\\) Number\\s+:\\s+([^ ]+)");
						} else if (IRegexUtils.isMatch(line, "Connector\\s+Type\\s+[: ]\\s+(.*)")) {
							type = IRegexUtils.getFirstGrop(line,
									"Connector\\s+Type\\s+[: ]\\s+(.*)");
						} else if (IRegexUtils.isMatch(line, "EEPROM\\s+contents\\s+\\(hex\\):")) {
							if ("mother".equals(cardtype)) {
								cardref = setCardRef(cardref, slotnum, -1, desc,
										hwVer, snum, pnum, frupnum, dslotname, mslotnum, mslotType);
							} else if ("daughter".equals(cardtype)) {
								cardref = setCardRef(cardref, slotnum, dslotnum, desc,
										hwVer, snum, pnum, frupnum, dslotname, mslotnum, mslotType);
							}
							cardtype = null;
						} else if (IRegexUtils.isMatch(line,
								"Controller\\s+Memory\\s+Size:\\s+(\\d+)\\s+MBytes")) {
							int val = IRegexUtils.extractInt(line,
									"Controller\\\\s+Memory\\\\s+Size:\\\\s+(\\\\d+)\\\\s+MBytes");
							if (val > 0 && !IUtils.isNull(cardref)) {
								try {
									((JSONObject) cardref.get(
											String.valueOf(slotnum))).put(
													"memory", new JSONObject().put("size", val));
								} catch (JSONException e) {
									// Ignore it
								}
							}
						}
					}
				}
				/*
				 * 	foreach my $key ( keys %$cardref ) {
						my @dcs;
						# pull off any daughter cards
						if ( defined $cardref->{$key}->{daughterCard} ) {
							foreach my $dc ( keys %{ $cardref->{$key}->{daughterCard} } ) {
								push( @dcs, $cardref->{$key}->{daughterCard}->{$dc} );
							}
							$cardref->{$key}->{daughterCard} = \@dcs if ( @dcs > 0 );
						}
						$out->print_element( "card", $cardref->{$key} );
					}
				 */
				IZTUtils.addInJson(cards, "cardref", cardref);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (!IUtils.isNull(reader)) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
		}
		//}
		return cards;
	}

	/**
	 * Method to fill cardRef object
	 * @param ref 
	 * @param slotnum
	 * @param dslotnum
	 * @param desc
	 * @param hwVer
	 * @param snum
	 * @param pnum
	 * @param frupnum
	 * @param dslotname
	 * @param mslotType
	 * @return
	 */
	private static JSONObject setCardRef(JSONObject ref, int slotnum, int dslotnum, String desc,
			String hwVer, String snum, String pnum, String frupnum, String dslotname, int mslotnum,
			String mslotType) {
		if (IUtils.isNull(ref)) {
			ref = new JSONObject();
		}
		JSONObject jslot = new JSONObject();
		JSONObject jdcard = new JSONObject();
		JSONObject jdslot = new JSONObject();
		JSONObject jasset = new JSONObject();
		JSONObject jfact = new JSONObject();
		if (!IUtils.isNullOrEmpty(desc)) {
			IZTUtils.addInJson(jdslot, "core:description", desc);
		}
		IZTUtils.addInJson(jfact, "core:make", "Cisco");
		IZTUtils.addInJson(jfact, "core:modelNumber", "Unknown");
		if (!IUtils.isNullOrEmpty(hwVer)) {
			IZTUtils.addInJson(jfact, "core:hardwareVersion", hwVer);
		}
		if (!IUtils.isNullOrEmpty(snum)) {
			IZTUtils.addInJson(jfact, "core:serialNumber", snum);
		}
		if (!IUtils.isNullOrEmpty(pnum)) {
			IZTUtils.addInJson(jfact, "core:partNumber", pnum);
		}
		if (!IUtils.isNullOrEmpty(frupnum)) {
			IZTUtils.addInJson(jfact, "core:fruPartNumber", frupnum);
		}
		IZTUtils.addInJson(jasset, "core:assetType", "Card");
		// create tree
		IZTUtils.addInJson(jasset, "core:factoryinfo", jfact);
		if (dslotnum >= 0) {
			IZTUtils.addInJson(jdslot, "core:asset", jasset);
			IZTUtils.addInJson(jdslot, "slotNumber",
					(dslotnum > 99 ? dslotnum - 100 : dslotnum));
			if (!IUtils.isNullOrEmpty(dslotname)) {
				IZTUtils.addInJson(jdslot, "slotName", dslotname);
			}
			if (!IUtils.isNullOrEmpty(mslotType)) {
				IZTUtils.addInJson(jdslot, "slotType", mslotType);
			}
			IZTUtils.addInJson(jdcard, String.valueOf(dslotnum), jdslot);
			IZTUtils.addInJson(jslot, "daughterCard", jdcard);
		} else {
			IZTUtils.addInJson(jslot, "slotNumber", mslotnum);
			IZTUtils.addInJson(jslot, "slotType", mslotType);
			IZTUtils.addInJson(jslot, "core:asset", jasset);
		}
		IZTUtils.addInJson(ref, String.valueOf(slotnum), jslot);
		return ref;
	}

	/**
	 * Method to parse file system object
	 * @param fs
	 * @param fSysCmdRes 
	 * @return
	 */
	public static JSONArray parseFileStorage(String fs, JSONObject fSysCmdRes) {
		JSONArray arr = null;
		if (!IUtils.isNullOrEmpty(fs)) {
			arr = new JSONArray();
			List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(fs,
					"^\\*?\\s+(\\d+)\\s+(\\d+)\\s+\\b(\\S+)\\b\\s+[A-Za-z]+\\s+(\\S+):",
					Pattern.MULTILINE);
			if (!IUtils.isNull(lst)) {
				for (List<String> l : lst) {
					if (!IUtils.isNull(l) && l.size() >= 5) {
						JSONObject jfs = new JSONObject();
						String storType = l.get(3);
						String name = l.get(4);
						IZTUtils.addInJson(jfs, "size", l.get(1));
						IZTUtils.addInJson(jfs, "freeSpace", l.get(2));
						IZTUtils.addInJson(jfs, "storageType", storType);
						IZTUtils.addInJson(jfs, "name", name);
						if (!IUtils.isNullOrEmpty(storType) &&
								!IRegexUtils.isMatch(storType, "opaque|nvram")) {
							if (!IUtils.isNull(fSysCmdRes) && fSysCmdRes.has(name)) {
								try {
									processShowFSResponse(jfs, fSysCmdRes.getString(name));
								} catch (JSONException e) {
									// ignore it
								}
							}
						}
						arr.put(jfs);
					}
				}
			}
		}
		return arr;
	}

	/**
	 * Method to process show <drive> response
	 * @param jfs
	 * @param show
	 */
	private static void processShowFSResponse(JSONObject jfs, String show) {
		if (!IUtils.isNull(jfs) && !IUtils.isNullOrEmpty(show)) {
			JSONObject rootDir = new JSONObject();
			JSONArray jArr = null;
			// 'show flash' format output
			if (IRegexUtils.isMatch(show,
					"^\\s*File\\s+Length\\s+Name\\/status\\s*$", Pattern.MULTILINE)) {
				List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(show,
						"^\\s*(\\d+)\\s+(\\d+)\\s+(\\S+)\\s*$", Pattern.MULTILINE);
				jArr = new JSONArray();
				for (List<String> l : lst) {
					if (!IUtils.isNull(l) && l.size() >= 4) {
						JSONObject file = new JSONObject();
						IZTUtils.addInJson(file, "size", l.get(2));
						IZTUtils.addInJson(file, "name", l.get(3));
						jArr.put(file);
					}
				}
				long size = IRegexUtils.extractInt(IRegexUtils.getFirstGrop(show,
						"^\\s*(\\d+)K bytes of processor board", Pattern.MULTILINE));
				if (size > 0) {
					IZTUtils.addInJson(jfs, "size", (size * 1024));
				}
			} else if (IRegexUtils.isMatch(show, // 'show disk' format output
					"^\\s*-#-\\s--length--\\s-----date\\/time------\\spath\\s*$",
					Pattern.MULTILINE)) {
				List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(show,
						"^\\s*\\d+\\s+(\\d+)\\s(?:.+?\\s){4}(\\S+)\\s*$", Pattern.MULTILINE);
				jArr = new JSONArray();
				for (List<String> l : lst) {
					if (!IUtils.isNull(l) && l.size() >= 3) {
						JSONObject file = new JSONObject();
						IZTUtils.addInJson(file, "size", l.get(1));
						IZTUtils.addInJson(file, "name", l.get(2));
						jArr.put(file);
					}
				}
			} else if (IRegexUtils.isMatch(show, // 'show bootflash' format output
					"^\\s*-#-\\sED\\s.+?type.+?crc.+?seek.+?nlen.+?length.+?date\\/time.+name\\s*$",
					Pattern.MULTILINE)) {
				List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(show,
						"^\\s*\\d+\\s+(?:\\S+\\s+){5}(\\d+)\\s(?:\\S+\\s+){5}(\\S+)\\s*$", Pattern.MULTILINE);
				jArr = new JSONArray();
				for (List<String> l : lst) {
					if (!IUtils.isNull(l) && l.size() >= 3) {
						JSONObject file = new JSONObject();
						IZTUtils.addInJson(file, "size", l.get(1));
						IZTUtils.addInJson(file, "name", l.get(2));
						jArr.put(file);
					}
				}
			}
			IZTUtils.addInJson(rootDir, "file", jArr);
			// add into file system json
			IZTUtils.addInJson(jfs, "rootDir", rootDir);
		}
	}

	/**
	 * Helper method to add items into json object array
	 * @param jMem
	 * @param kind
	 * @param size
	 * @param addDesc 
	 */
	public static void addFlashMem(JSONArray jMem, String kind, int size, boolean addDesc) {
		JSONObject jobj = new JSONObject();
		IZTUtils.addInJson(jobj, "kind", kind);
		IZTUtils.addInJson(jobj, "size", (size * 1024));
		if (addDesc) {
			IZTUtils.addInJson(jobj, "core:description", kind);
		}
		jMem.put(jobj);
	}

	/**
	 * Method to parse power supply.
	 * @param power
	 * @param inventory 
	 * @param version 
	 * @return
	 */
	public static JSONArray parsePower(String power, String inventory, String version) {
		JSONArray arr = null;
		if (!IUtils.isNullOrEmpty(power)) {
			if (IRegexUtils.isMatch(power, "Supply\\s+Model\\s+No\\s+Type\\s+Status")) {
				List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(power,
						"^PS(\\d+)\\s+(\\S+)\\s+(\\S+.+?W\\b)\\s+(\\S+)", Pattern.MULTILINE);
				if (!IUtils.isNull(lst) && lst.size() > 0) {
					arr = new JSONArray();
					for (List<String> match : lst) {
						if (!IUtils.isNull(match) && match.size() >= 5) {
							JSONObject json = new JSONObject();
							IZTUtils.addInJson(json, "number", match.get(1));
							JSONObject asset = new JSONObject();
							IZTUtils.addInJson(asset, "core:assetType", "PowerSupply");
							JSONObject fact = new JSONObject();
							IZTUtils.addInJson(fact, "core:make", "Unknown");
							IZTUtils.addInJson(fact, "core:modelNumber", "Unknown");
							IZTUtils.addInJson(fact, "core:partNumber", match.get(2));
							IZTUtils.addInJson(json, "core:description", match.get(3));
							if (!IUtils.isNullOrEmpty(match.get(4)) &&
									"good".equals(match.get(4))) {
								IZTUtils.addInJson(json, "status", "OK");
							} else {
								IZTUtils.addInJson(json, "status", "Fault");
							}
							IZTUtils.addInJson(asset, "core:factoryinfo", fact);
							IZTUtils.addInJson(json, "core:asset", asset);
							arr.put(json);
						}
					}
				}
			} else {
				List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(power,
						"(\\d+)\\s+(\\S+)\\s+(\\d+.\\d\\d)\\s+(\\d+.\\d\\d)\\s+(OK|\\S+)\\s+(OK|\\S+)\\s+(on|off)",
						Pattern.MULTILINE);
				if (!IUtils.isNull(lst) && lst.size() > 0) {
					arr = new JSONArray();
					for (List<String> match : lst) {
						if (!IUtils.isNull(match) && match.size() >= 7) {
							JSONObject json = new JSONObject();
							IZTUtils.addInJson(json, "number", match.get(1));
							JSONObject asset = new JSONObject();
							IZTUtils.addInJson(asset, "core:assetType", "PowerSupply");
							JSONObject fact = new JSONObject();
							IZTUtils.addInJson(fact, "core:make", "Unknown");
							IZTUtils.addInJson(fact, "core:modelNumber", "Unknown");
							IZTUtils.addInJson(fact, "core:partNumber", match.get(2));
							if (!IUtils.isNullOrEmpty(inventory)) {
								List<String> list = IRegexUtils.getMatchingGrops(Pattern.MULTILINE, inventory,
								"PS\\s+" + match.get(1) + "\\b.+?DESCR:\\s\"(.+?)\".+?SN:\\s+(\\S+)");
								if (!IUtils.isNull(list) && list.size() >= 3) {
									IZTUtils.addInJson(json, "core:description", match.get(1));
									IZTUtils.addInJson(fact, "core:serialNumber", match.get(2));
								}
							}
							if (!IUtils.isNullOrEmpty(match.get(6)) &&
									"OK".equals(match.get(6))) {
								IZTUtils.addInJson(json, "status", "OK");
							} else {
								IZTUtils.addInJson(json, "status", "Fault");
							}
							IZTUtils.addInJson(asset, "core:factoryinfo", fact);
							IZTUtils.addInJson(json, "core:asset", asset);
							arr.put(json);
						}
					}
				}
			}
		}
		if (IUtils.isNull(arr) || arr.length() == 0) {
			String num = IRegexUtils.getFirstGrop(version,
					"Power\\s+supply\\s+part\\s+number\\s*:\\s+(\\S+)",
					Pattern.CASE_INSENSITIVE);
			if (!IUtils.isNullOrEmpty(num)) {
				arr = new JSONArray();
				JSONObject json = new JSONObject();
				IZTUtils.addInJson(json, "number", 1);
				JSONObject asset = new JSONObject();
				IZTUtils.addInJson(asset, "core:assetType", "PowerSupply");
				JSONObject fact = new JSONObject();
				IZTUtils.addInJson(fact, "core:make", "Unknown");
				IZTUtils.addInJson(fact, "core:modelNumber", "Unknown");
				IZTUtils.addInJson(fact, "core:partNumber", num);
				IZTUtils.addInJson(fact, "core:serialNumber",
						IRegexUtils.getFirstGrop(version,
								"Power\\s+supply\\s+serial\\s+number\\s*:\\s+(\\S+)"));
				IZTUtils.addInJson(asset, "core:factoryinfo", fact);
				IZTUtils.addInJson(json, "core:asset", asset);
				arr.put(json);
			}
		}
		return arr;
	}

	/**
	 * Method to create config object from strings.
	 * @param runConfig
	 * @param start_config
	 * @return
	 */
	public static JSONObject createConfig(String runConfig, String start_config) {
		JSONObject json = new JSONObject();
		IZTUtils.addInJson(json, "core:name", "/");
		JSONArray arr = new JSONArray();
		JSONObject run = new JSONObject();
		IZTUtils.addInJson(run, "core:name", "running-config");
		IZTUtils.addInJson(run, "core:mediaType", "text/plain");
		IZTUtils.addInJson(run, "core:context", "active");
		IZTUtils.addInJson(run, "core:promotable", "false");
		IZTUtils.addInJson(run, "core:textBlob", Base64.getEncoder().encode(
				applyMask(runConfig).getBytes()));
		arr.put(run);
		if (!IUtils.isNull(start_config)) {
			JSONObject start = new JSONObject();
			IZTUtils.addInJson(start, "core:name", "startup-config");
			IZTUtils.addInJson(start, "core:mediaType", "text/plain");
			IZTUtils.addInJson(start, "core:context", "active");
			IZTUtils.addInJson(start, "core:promotable", "false");
			IZTUtils.addInJson(start, "core:textBlob", Base64.getEncoder().encode(
					applyMask(start_config).getBytes()));
			arr.put(start);
		}
		IZTUtils.addInJson(json, "core:config", arr);
		return json;
	}

	/**
	 * Method to process config
	 * @param config
	 * @return
	 */
	private static String applyMask(String config) {
		if (!IUtils.isNullOrEmpty(config)) {
			// remove the variable ntp clock-period line
			String ret = config.replaceFirst("\\n^ntp clock-period.+$", "");
			// remove the NVRAM updated time comment
			ret = ret.replaceAll("\\n^! NVRAM config last updated at.+$", "");
			return ret;
		}
		return null;
	}

	/**
	 * Method to parse access ports.
	 * @param runConfig
	 * @return
	 */
	public static JSONObject parseAccessPorts(String runConfig) {
		JSONObject json = null;
		if (!IUtils.isNullOrEmpty(runConfig)) {
			List<List<String>> list = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"\\bline\\s+(vty|aux|con)?\\s*(\\d+)\\s*(\\d*)(.+?)(?=^\\S)",
					Pattern.MULTILINE);
			JSONArray arr = new JSONArray();
			if (!IUtils.isNull(list)) {
				for (List<String> lst : list) {
					if (!IUtils.isNull(lst) && lst.size() >= 5) {
						JSONObject port = new JSONObject();
						String type = "unknown";
						if (!IUtils.isNullOrEmpty(lst.get(1))) {
							type = lst.get(1);
						}
						IZTUtils.addInJson(port, "type", type);
						IZTUtils.addInJson(port, "startInstance", lst.get(2));
						if (!IUtils.isNullOrEmpty(lst.get(3))) {
							IZTUtils.addInJson(port, "endInstance", lst.get(3));
						}
						String blob = lst.get(4);
						if (!IUtils.isNullOrEmpty(blob)) {
							List<String> l = IRegexUtils.getMatchingGrops(Pattern.MULTILINE,
									blob, "^\\s+exec-timeout\\s+(\\d+)\\s+(\\d+)");
							if (!IUtils.isNull(l) && l.size() >= 3) {
								IZTUtils.addInJson(port, "inactivityTimeout",
										((IRegexUtils.extractInt(l.get(1)) * 60 )+ 
												IRegexUtils.extractInt(l.get(2))));
							}
							l = IRegexUtils.getMatchingGrops(Pattern.MULTILINE,
									blob, "^\\s+session-timeout\\s+(\\d+)\\s+(\\d+)");
							if (!IUtils.isNull(l) && l.size() >= 3) {
								IZTUtils.addInJson(port, "sessionTimeout",
										((IRegexUtils.extractInt(l.get(1)) * 60 )+ 
												IRegexUtils.extractInt(l.get(2))));
							}
							l = IRegexUtils.getMatchingGrops(Pattern.MULTILINE,
									blob, "^\\s+absolute-timeout\\s+(\\d+)\\s+(\\d+)");
							if (!IUtils.isNull(l) && l.size() >= 3) {
								IZTUtils.addInJson(port, "absoluteTimeout",
										((IRegexUtils.extractInt(l.get(1)) * 60 )+ 
												IRegexUtils.extractInt(l.get(2))));
							}
							IZTUtils.addInJson(port, "inboundProtocol",
									IRegexUtils.getFirstGrop(blob,
											"^\\s+transport\\s+input\\s+(.*\\S)\\s*$", Pattern.MULTILINE));
							IZTUtils.addInJson(port, "outboundProtocol",
									IRegexUtils.getFirstGrop(blob,
											"^\\s+transport\\s+output\\s+(.*\\S)\\s*$", Pattern.MULTILINE));
							l = IRegexUtils.getMatchingGrops(Pattern.MULTILINE,
									blob, "^\\s+access-class\\s+(\\S+)\\s+(in|out)\\s*$");
							if (!IUtils.isNull(l) && l.size() >= 3) {
								String key = null;
								if (!IUtils.isNullOrEmpty(l.get(2)) &&
										l.get(2).contains("out")) {
									key = "egressFilter";
								} else {
									key = "ingressFilter";
								}
								IZTUtils.addInJson(port, key, l.get(1));
							}
						}
						arr.put(port);
					}
				}
			}
			if (IUtils.isNull(json))
				json = new JSONObject();
			IZTUtils.addInJson(json, "accessPort", arr);
		}
		return json;
	}

	/**
	 * Method to parse filters
	 * @param access_lists
	 * @return
	 */
	public static JSONObject parseFilters(String access_lists) {
		if (!IUtils.isNullOrEmpty(access_lists)) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new StringReader(access_lists));
				JSONObject obj = new JSONObject();
				JSONObject thisacl = new JSONObject();
				int term_process_order = 0;
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (IRegexUtils.isMatch(line, "^access-list.*\\s+remark\\s+") ||
							IRegexUtils.isMatch(line, "show access-list") ||
							IRegexUtils.isMatch(line, "^\\s*$") ||
							IRegexUtils.isMatch(line, "^\\s*\\S+\\s*$")) {
						continue;
					} else {
						List<String> list = IRegexUtils.getMatchingGrops(
								Pattern.CASE_INSENSITIVE, line,
								"^(standard|extended)\\s+(ip)\\s+access\\s+list\\s+(\\S+)\\s*$");
						if (!IUtils.isNull(list) && list.size() >= 4) {
							if (thisacl.has("name")) {
								IZTUtils.addInJson(obj, "name", thisacl);
								thisacl = new JSONObject();
								term_process_order = 0;
							}
							IZTUtils.addInJson(thisacl, "type", list.get(1) + " IP");
							IZTUtils.addInJson(thisacl, "protocol", list.get(2).toLowerCase());
							IZTUtils.addInJson(thisacl, "name", list.get(3));
						} else {
							list = IRegexUtils.getMatchingGrops(
									Pattern.CASE_INSENSITIVE, line,
									"^([^\\s].*)\\s+access\\s+list\\s+([^\\s^:]+)(:|)");
							if (!IUtils.isNull(list) && list.size() >= 3) {
								if (thisacl.has("name")) {
									IZTUtils.addInJson(obj, "name", thisacl);
									thisacl = new JSONObject();
									term_process_order = 0;
								}
								IZTUtils.addInJson(thisacl, "type", list.get(2));
								IZTUtils.addInJson(thisacl, "name", list.get(1));
							}
						}
						if (thisacl.has("name") && !thisacl.has("protocol")) {
							continue;
						} else if (IRegexUtils.isMatch(line, "^\\s*(\\d+\\s+)?(permit|deny|dynamic)")) {
							if (!thisacl.has("type")) {
								continue;
							}
							if (IRegexUtils.isMatch(line, "^\\s*\\d+\\s+(permit|deny|dynamic)")) {
								line = line.replaceAll("^\\s*\\d+\\s+", "");
							}
							if (thisacl.optString("type", "").toLowerCase().contains("standard")) {
								term_process_order = standardTerm(thisacl, line, term_process_order);
								term_process_order++;
							} else if (thisacl.optString("type", "").toLowerCase().contains("extended")) {
								term_process_order = extendedTerm(thisacl, line, term_process_order);
								term_process_order++;
							}
						}
					}
				}
				if (thisacl.has("name")) {
					IZTUtils.addInJson(obj, "name", thisacl);
				}
				return obj;
			} catch (IOException e) {
				logger.error(e.getMessage());
			} finally {
				if (!IUtils.isNull(reader)) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return null;
	}

	/**
	 * Method to process extended term
	 * @param json
	 * @param line
	 * @param term_process_order
	 * @return
	 */
	private static int extendedTerm(JSONObject json, String line,
			int term_process_order) {
		JSONObject term = new JSONObject();
		IZTUtils.addInJson(term, "processOrder", term_process_order);
		String primaryAction = IRegexUtils.getFirstGrop(line,
				"^\\s*(no permit|deny|permit)");
		if (IUtils.isNullOrEmpty(primaryAction)) {
			primaryAction = IRegexUtils.getMatchGrop(3, line,
					"^\\s*dynamic\\s+(\\S+)\\s+timeout\\s+(\\S+)\\s+(no permit|deny|permit)",
					Pattern.CASE_INSENSITIVE);
			if (IUtils.isNullOrEmpty(primaryAction)) {
				primaryAction = IRegexUtils.getMatchGrop(2, line,
						"^\\s*dynamic\\s+(\\S+)\\s+timeout\\s+(\\S+)\\s+(no permit|deny|permit)",
						Pattern.CASE_INSENSITIVE);
			}
		}
		if (!IUtils.isNullOrEmpty(primaryAction)) {
			if ("no permit".equals(primaryAction.toLowerCase())) {
				primaryAction = "deny";
			}
			IZTUtils.addInJson(term, "primaryAction", primaryAction);
		}
		String proto = IRegexUtils.getMatchGrop(2, line,
				"^\\s*(no permit|deny|permit)\\s+(\\S+)\\s+", Pattern.CASE_INSENSITIVE);
		if (!IUtils.isNullOrEmpty(proto)) {
			IZTUtils.addInJson(term, "protocol", proto);
		}
		String srctext = IRegexUtils.getFirstGrop(line,
				"^(\\s*(no permit|deny|permit)\\s+\\S+\\s+any)\\s+");
		if (!IUtils.isNullOrEmpty(srctext)) {
			JSONObject ipAdd = new JSONObject();
			IZTUtils.addInJson(ipAdd, "address", "0.0.0.0");
			IZTUtils.addInJson(ipAdd, "mask", "255.255.255.255");
			IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
		} else {
			List<String> list = IRegexUtils.getMatchingGrops(line,
					"^(\\s*(no permit|deny|permit)\\s+\\S+\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+))\\s+");
			if (!IUtils.isNull(list) && list.size() >= 5) {
				srctext = list.get(1);
				JSONObject ipAdd = new JSONObject();
				IZTUtils.addInJson(ipAdd, "address", list.get(3));
				IZTUtils.addInJson(ipAdd, "mask", list.get(4));
				IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
			} else {
				list = IRegexUtils.getMatchingGrops(line,
						"^(\\s*(no permit|deny|permit)\\s+\\S+\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+),\\s+wildcard\\s+bits\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+))\\s+");
				if (!IUtils.isNull(list) && list.size() >= 5) {
					srctext = list.get(1);
					JSONObject ipAdd = new JSONObject();
					IZTUtils.addInJson(ipAdd, "address", list.get(3));
					IZTUtils.addInJson(ipAdd, "mask", list.get(4));
					IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
				} else {
					list = IRegexUtils.getMatchingGrops(line,
							"^(\\s*(no permit|deny|permit)\\s+\\S+\\s+host\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+))\\s+");
					if (!IUtils.isNull(list) && list.size() >= 4) {
						srctext = list.get(1);
						JSONObject ipAdd = new JSONObject();
						IZTUtils.addInJson(ipAdd, "address", list.get(3));
						IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
					} else {
						list = IRegexUtils.getMatchingGrops(line,
								"^(\\s*(no permit|deny|permit)\\s+\\S+\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+))\\s+");
						if (!IUtils.isNull(list) && list.size() >= 4) {
							srctext = list.get(1);
							JSONObject ipAdd = new JSONObject();
							IZTUtils.addInJson(ipAdd, "address", list.get(3));
							IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
						}
					}
				}
			}
		}
		String srcport = null;
		if (!IUtils.isNullOrEmpty(srctext)) {
			List<String> list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
					line, srctext + "\\s+((lt|gt|eq|neq)\\s+(\\S+))");
			if (!IUtils.isNull(list) && list.size() >= 4) {
				srcport = list.get(1);
				JSONObject port = new JSONObject();
				IZTUtils.addInJson(port, "operator", list.get(2));
				IZTUtils.addInJson(port, "portStart", getIntPort(list.get(3)));
				IZTUtils.addInJson(term, "sourceService", port);
			} else {
				list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
						line, srctext + "\\s+(range\\s+(\\S+)[-\\s](\\S+))");
				if (!IUtils.isNull(list) && list.size() >= 4) {
					JSONObject port = new JSONObject();
					IZTUtils.addInJson(port, "portStart", getIntPort(list.get(2)));
					IZTUtils.addInJson(port, "portEnd", getIntPort(list.get(3)));
					IZTUtils.addInJson(term, "sourceService", port);
				}
			}
		}
		String dsttext = null;
		String prefix = null;
		if (!IUtils.isNullOrEmpty(srctext)) {
			prefix = srctext + "\\s*";
			if (IUtils.isNullOrEmpty(srcport)) {
				prefix += "\\S+";
			} else {
				prefix += srcport;
			}
			dsttext = IRegexUtils.getFirstGrop(line, prefix + "\\s*(any)");
			if (!IUtils.isNullOrEmpty(dsttext)) {
				JSONObject ipAdd = new JSONObject();
				IZTUtils.addInJson(ipAdd, "address", "0.0.0.0");
				IZTUtils.addInJson(ipAdd, "mask", "255.255.255.255");
				IZTUtils.addInJson(term, "destinationIpAddr", ipAdd);
			} else {
				List<String> list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
						line, prefix + "\\s*((\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+))");
				if (!IUtils.isNull(list) && list.size() >= 4) {
					dsttext = list.get(1);
					JSONObject ipAdd = new JSONObject();
					IZTUtils.addInJson(ipAdd, "address", list.get(2));
					IZTUtils.addInJson(ipAdd, "mask", list.get(3));
					IZTUtils.addInJson(term, "destinationIpAddr", ipAdd);
				} else {
					list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
							line, prefix + "\\s*((\\d+\\.\\d+\\.\\d+\\.\\d+),\\s+wildcard\\s+bits\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+))");
					if (!IUtils.isNull(list) && list.size() >= 4) {
						dsttext = list.get(1);
						JSONObject ipAdd = new JSONObject();
						IZTUtils.addInJson(ipAdd, "address", list.get(2));
						IZTUtils.addInJson(ipAdd, "mask", list.get(3));
						IZTUtils.addInJson(term, "destinationIpAddr", ipAdd);
					} else {
						list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
								line, prefix + "\\s*(host\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+))");
						if (!IUtils.isNull(list) && list.size() >= 3) {
							dsttext = list.get(1);
							JSONObject ipAdd = new JSONObject();
							IZTUtils.addInJson(ipAdd, "address", list.get(2));
							IZTUtils.addInJson(term, "destinationIpAddr", ipAdd);
						} else {
							list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									line, prefix + "\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)");
							if (!IUtils.isNull(list) && list.size() >= 2) {
								dsttext = list.get(1);
								JSONObject ipAdd = new JSONObject();
								IZTUtils.addInJson(ipAdd, "address", list.get(1));
								IZTUtils.addInJson(term, "destinationIpAddr", ipAdd);
							}
						}
					}
				}
			}
		}
		if (!IUtils.isNullOrEmpty(dsttext)) {
			prefix += "\\s*" + dsttext;
			List<String> list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
					line, prefix + "\\s+((lt|gt|eq|neq)\\s+(\\S+))");
			if (!IUtils.isNull(list) && list.size() >= 4) {
				JSONObject port = new JSONObject();
				IZTUtils.addInJson(port, "portStart", list.get(3));
				IZTUtils.addInJson(port, "operator", list.get(2));
				IZTUtils.addInJson(term, "destinationService", port);
			} else {
				list = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
						line, prefix + "\\s+(range\\s+(\\S+)[-\\s](\\S+))");
				if (!IUtils.isNull(list) && list.size() >= 4) {
					JSONObject port = new JSONObject();
					IZTUtils.addInJson(port, "portStart", list.get(2));
					IZTUtils.addInJson(port, "portEnd", list.get(3));
					IZTUtils.addInJson(term, "destinationService", port);
				}
			}
		}
		if (IRegexUtils.isMatch(line, "\\s+log\\s*")) {
			IZTUtils.addInJson(term, "otherAction", "log");
			IZTUtils.addInJson(term, "log", true);
		} else {
			IZTUtils.addInJson(term, "log", false);
		}
		IZTUtils.addInJson(json, "filterEntry", term);
		return term_process_order;
	}

	/**
	 * Method to get protocol port from string
	 * @param val
	 * @return
	 */
	private static String getIntPort(String val) {
		if (!IUtils.isNullOrEmpty(val)) {
			if (val.matches("^\\d+$")) {
				return val;
			} else {
				try {
					val = val.replaceAll("-", "");
					IZTUtils.PROTOCOLS proto = IZTUtils.PROTOCOLS.valueOf(val.toUpperCase());
					return String.valueOf(proto.getPort());
				} catch (Exception e) {}
			}
		}
		return null;
	}

	/**
	 * Method to process standard term
	 * @param json
	 * @param line
	 * @param term_process_order
	 * @return
	 */
	private static int standardTerm(JSONObject json, String line,
			int term_process_order) {
		JSONObject term = new JSONObject();
		IZTUtils.addInJson(term, "processOrder", term_process_order);
		String primaryAction = IRegexUtils.getFirstGrop(line,
				"^\\s*(no permit|permit|deny)\\s+(any)");
		if (!IUtils.isNullOrEmpty(primaryAction)) {
			JSONObject ipAdd = new JSONObject();
			IZTUtils.addInJson(ipAdd, "address", "0.0.0.0");
			IZTUtils.addInJson(ipAdd, "mask", "255.255.255.255");
			IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
		} else {
			List<String> list = IRegexUtils.getMatchingGrops(line,
					"^\\s*(no permit|permit|deny)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");
			if (!IUtils.isNull(list) && list.size() >= 4) {
				primaryAction = list.get(1);
				JSONObject ipAdd = new JSONObject();
				IZTUtils.addInJson(ipAdd, "address", list.get(2));
				IZTUtils.addInJson(ipAdd, "mask", list.get(3));
				IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
			} else {
				list = IRegexUtils.getMatchingGrops(line,
						"^\\s*(no permit|permit|deny)\\s+host\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");
				if (!IUtils.isNull(list) && list.size() >= 3) {
					primaryAction = list.get(1);
					JSONObject ipAdd = new JSONObject();
					IZTUtils.addInJson(ipAdd, "address", list.get(2));
					IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
				} else {
					list = IRegexUtils.getMatchingGrops(line,
							"^\\s*(no permit|permit|deny)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+),\\s+wildcard\\s+bits\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");
					if (!IUtils.isNull(list) && list.size() >= 4) {
						primaryAction = list.get(1);
						JSONObject ipAdd = new JSONObject();
						IZTUtils.addInJson(ipAdd, "address", list.get(2));
						IZTUtils.addInJson(ipAdd, "mask", list.get(3));
						IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
					} else {
						list = IRegexUtils.getMatchingGrops(line,
								"^\\s*(no permit|permit|deny)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)");
						if (!IUtils.isNull(list) && list.size() >= 3) {
							primaryAction = list.get(1);
							JSONObject ipAdd = new JSONObject();
							IZTUtils.addInJson(ipAdd, "address", list.get(2));
							IZTUtils.addInJson(term, "sourceIpAddr", ipAdd);
						}
					}
				}
			}
		}
		if (IRegexUtils.isMatch(line, "\\s+log\\s*$")) {
			IZTUtils.addInJson(term, "log", true);
		} else {
			IZTUtils.addInJson(term, "log", false);
		}
		if (!IUtils.isNullOrEmpty(primaryAction) &&
				"no permit".equals(primaryAction.toLowerCase())) {
			primaryAction = "deny";
		}
		IZTUtils.addInJson(term, "primaryAction", primaryAction);
		IZTUtils.addInJson(json, "filterEntry", term);
		return term_process_order;
	}

	/**
	 * Method to parser interfaces
	 * @param runConfig 
	 * @param interfaces
	 * @param ospf
	 * @return
	 */
	public static JSONObject parseInterfaces(String runConfig, String interfaces, String ospf) {
		JSONObject jifs = new JSONObject();
		JSONArray ifs = null;
		if (!IUtils.isNullOrEmpty(interfaces)) {
			ifs = new JSONArray();
			List<List<String>> list = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"(?s)^interface\\s+(\\S+)(.+?)^!", Pattern.MULTILINE);
			for (List<String> lst : list) {
				JSONObject intrfc = null;
				if (!IUtils.isNull(lst) && lst.size() >= 3) {
					String name = lst.get(1);
					String blob = lst.get(2);
					intrfc = new JSONObject();
					IZTUtils.addInJson(intrfc, "name", name);
					IZTUtils.addInJson(intrfc, "interfaceType", IZTUtils.getInterfaceType(name));
					IZTUtils.addInJson(intrfc, "physical", isPhysical(name));
					String desc = IRegexUtils.getFirstGrop(blob, "^\\s*description\\s+(.+?)\\s*$",
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					if (!IUtils.isNullOrEmpty(desc)) {
						IZTUtils.addInJson(intrfc, "description", desc);
					}
					IZTUtils.addInJson(intrfc, "interfaceIp", parseIP(blob));
					String acl = IRegexUtils.getFirstGrop(blob,
							"^\\s*ip access-group\\s*\\b(.+?)\\b\\s+(in|out)\\s*$",
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					String dir = IRegexUtils.getMatchGrop(2, blob,
							"^\\s*ip access-group\\s*\\b(.+?)\\b\\s+(in|out)\\s*$",
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
					if (!IUtils.isNullOrEmpty(dir)) {
						if ("out".equals(dir)) {
							IZTUtils.addInJson(intrfc, "egressFilter", acl);
						} else if ("in".equals(dir)) {
							IZTUtils.addInJson(intrfc, "ingressFilter", acl);
						}
					}
					if (!IUtils.isNullOrEmpty(name) && name.toLowerCase().matches("eth")) {
						JSONObject obj = parseEthernet(blob);
						IZTUtils.addInJson(intrfc, "interfaceEthernet", obj);
					}
					if (!IUtils.isNullOrEmpty(name) &&
							IRegexUtils.isMatch(name, "^pos(\\d|\\s+\\d)")) {
						IZTUtils.addInJson(intrfc, "interfaceSonet", parseSonet(blob));
					}
					// serial
					if (!IUtils.isNullOrEmpty(name) &&
							IRegexUtils.isMatch(name, "^serial(\\d|\\s+\\d)")) {
						IZTUtils.addInJson(intrfc, "interfaceSerial", parseSerial(blob));
					}
					// ATM
					if (!IUtils.isNullOrEmpty(name) &&
							IRegexUtils.isMatch(name, "^atm(\\d|\\s+\\d)")) {
						IZTUtils.addInJson(intrfc, "interfaceATM", parseAtm(blob));
					}
					// ISDN (as BRI)
					if (!IUtils.isNullOrEmpty(name) &&
							IRegexUtils.isMatch(name, "^bri(\\d|\\s+\\d)")) {
						IZTUtils.addInJson(intrfc, "interfaceISDN", parseIsdnBri(blob));
					}
					// PPP
					IZTUtils.addInJson(intrfc, "interfacePPP", parsePPP(blob));
					// IPX and IPX addresses
					IZTUtils.addInJson(intrfc, "interfaceIPX", parseIPX(blob));
					// FRAME RELAY
					IZTUtils.addInJson(intrfc, "interfaceFrameRelay", parseFrameRelay(blob));
					// VLAN trunk references
					String vlandata = IRegexUtils.getFirstGrop(blob,
							"^\\s+switchport\\s+trunk\\s+allowed\\s+vlan\\s+(\\S+)");
					if (!IUtils.isNullOrEmpty(vlandata)) {
						Object vlan = null;
						if (vlandata.matches("\\d+")) {
							vlan = new JSONObject();
							IZTUtils.addInJson((JSONObject) vlan, "startVlan", vlandata);
						} else {
							vlan = new JSONArray();
							for (String s : vlandata.split(",")) {
								JSONObject json = new JSONObject();
								if (!IUtils.isNullOrEmpty(s) && s.contains("-")) {
									String[] arr = s.split("-");
									if (!IUtils.isNull(arr) && arr.length >= 2) {
										IZTUtils.addInJson(json, "startVlan", arr[0]);
										IZTUtils.addInJson(json, "endVlan", arr[1]);
									}
								} else {
									IZTUtils.addInJson(json, "startVlan", s);
								}
								// push in array
								((JSONArray) vlan).put(json);
							}
						}
						IZTUtils.addInJson(intrfc, "interfaceVlanTrunks", vlan);
					}
					// EIGRP
					JSONObject eigrp = null;
					List<String> leigrp = IRegexUtils.getMatchingGrops(
							Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, blob,
							"hello-interval eigrp (\\d+) (\\d+)");
					if (!IUtils.isNull(leigrp) && leigrp.size() >= 3) {
						eigrp = new JSONObject();
						IZTUtils.addInJson(eigrp, "cisco:asNumber", leigrp.get(1));
						IZTUtils.addInJson(eigrp, "cisco:helloInterval", leigrp.get(2));
					} else {
						leigrp = IRegexUtils.getMatchingGrops(
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, blob,
								"hold-time eigrp (\\d+) (\\d+)");
						if (!IUtils.isNull(leigrp) && leigrp.size() >= 3) {
							eigrp = new JSONObject();
							IZTUtils.addInJson(eigrp, "cisco:asNumber", leigrp.get(1));
							IZTUtils.addInJson(eigrp, "cisco:holdTime", leigrp.get(2));
						}
					}
					IZTUtils.addInJson(intrfc, "cisco:eigrp", eigrp);
					// process ospf interface results for name
					if (!IUtils.isNullOrEmpty(ospf)) {
						String ospfTxt = IRegexUtils.getFirstGrop(ospf,
								"^(" + name + "\\s.+?)(?=^\\S)",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
						if (!IUtils.isNullOrEmpty(ospfTxt)) {
							JSONObject jospf = new JSONObject();
							IZTUtils.addInJson(jospf, "area",
									IRegexUtils.getFirstGrop(ospfTxt,
											"Area (\\S+)", Pattern.CASE_INSENSITIVE));
							List<String> lospf = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									ospfTxt, "Process ID\\s+(\\d+),\\s*Router ID\\s+\\S+,\\s*Network Type\\s+(\\S+),\\s*Cost:\\s+(\\d+)");
							if (!IUtils.isNull(lospf) && lospf.size() >= 4) {
								IZTUtils.addInJson(jospf, "processId", lospf.get(1));
								IZTUtils.addInJson(jospf, "networkType", lospf.get(2));
								IZTUtils.addInJson(jospf, "cost", lospf.get(3));
							}
							lospf = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									ospfTxt, "Transmit Delay is (\\d+) sec,\\s*State (\\S+),\\s*(Priority (\\d+))?");
							if (!IUtils.isNull(lospf) && lospf.size() >= 3) {
								IZTUtils.addInJson(jospf, "transmitDelay", lospf.get(1));
								IZTUtils.addInJson(jospf, "routerState", lospf.get(2));
								if (lospf.size() >= 5) {
									IZTUtils.addInJson(jospf, "routerPriority", lospf.get(4));
								}
							}
							lospf = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									ospfTxt, "Hello\\s*(\\d+),\\s*Dead\\s*(\\d+),\\s*Wait\\s*(\\d+),\\s*Retransmit\\s*(\\d+)");
							if (!IUtils.isNull(lospf) && lospf.size() >= 5) {
								IZTUtils.addInJson(jospf, "helloInterval", lospf.get(1));
								IZTUtils.addInJson(jospf, "deadInterval", lospf.get(2));
								IZTUtils.addInJson(jospf, "waitInterval", lospf.get(3));
								IZTUtils.addInJson(jospf, "retransmitInterval", lospf.get(4));
							}
							IZTUtils.addInJson(intrfc, "interfaceOspf", jospf);
						}
					}
					// process interface from "show interfaces"
					if (!IUtils.isNullOrEmpty(interfaces)) {
						String intBlob = IRegexUtils.getFirstGrop(ospf,
								"^(" + name + "\\s.+?)(?=^\\S)",
								Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
						if (!IUtils.isNullOrEmpty(intBlob)) {
							JSONObject eth = intrfc.optJSONObject("interfaceEthernet");
							if (IUtils.isNull(eth)) {
								eth = new JSONObject();
							}
							List<String> lintrfc = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									intBlob, "^(" + name + "\\s+is\\s+((up)|(?:\\S+\\s+(down))|(down)),");
							if (!IUtils.isNull(lintrfc) && !lintrfc.isEmpty()) {
								IZTUtils.addInJson(intrfc, "adminStatus", (lintrfc.size() >= 5 ?
										lintrfc.get(4) : lintrfc.size() >= 3 ?
												lintrfc.get(2) : "up"));
							}
							lintrfc = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									intBlob, "address is ([a-f0-9A-F]{4}\\.[a-f0-9A-F]{4}\\.[a-f0-9A-F]{4})");
							if (!IUtils.isNull(lintrfc) && lintrfc.size() >= 2) {
								IZTUtils.addInJson(eth, "macAddress", IZTUtils.stripMac(lintrfc.get(1)));
							}
							lintrfc = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									intBlob, "MTU\\s+(\\d+)\\s+bytes,\\s+BW\\s+(\\d+)\\s+(\\S+),");
							if (!IUtils.isNull(lintrfc) && lintrfc.size() >= 4) {
								IZTUtils.addInJson(intrfc, "mtu", lintrfc.get(1));
								int speed = IRegexUtils.extractInt(lintrfc.get(2));
								if (!IUtils.isNullOrEmpty(lintrfc.get(3))) {
									if (lintrfc.get(3).toLowerCase().contains("mb")) {
										speed = speed * 1000 * 1000;
									} else if (lintrfc.get(3).toLowerCase().contains("mb")) {
										speed = speed * 1000;
									}
								}
								IZTUtils.addInJson(intrfc, "speed", speed);
							}
							lintrfc = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									intBlob, "MTU\\s+(\\d+)\\s+bytes,\\s+sub\\s+MTU\\s+\\d+,\\s+BW\\s+(\\d+)\\s+(\\S+),");
							if (!IUtils.isNull(lintrfc) && lintrfc.size() >= 4) {
								IZTUtils.addInJson(intrfc, "mtu", lintrfc.get(1));
								int speed = IRegexUtils.extractInt(lintrfc.get(2));
								if (!IUtils.isNullOrEmpty(lintrfc.get(3))) {
									if (lintrfc.get(3).toLowerCase().contains("mbit")) {
										speed = speed * 1000;
									}
								}
								IZTUtils.addInJson(intrfc, "speed", speed);
							}
							String encap = IRegexUtils.getFirstGrop(intBlob, "Encapsulation\\s([^\\s^,]+)");
							if (!IUtils.isNullOrEmpty(encap)) {
								if ("ethernet".equals(intrfc.optString("interfaceType"))) {
									IZTUtils.addInJson(eth, "encapsulation", encap);
								}
							}
							lintrfc = IRegexUtils.getMatchingGrops(Pattern.CASE_INSENSITIVE,
									intBlob, "(Half|Full|Auto)[- ]duplex,\\s+(100Mb|10Mb|Auto).+,\\s+(media type is |)(\\S+)");
							if (!IUtils.isNull(lintrfc) && lintrfc.size() >= 5) {
								IZTUtils.addInJson(eth, "mediaType", lintrfc.get(4));
								String duplex = lintrfc.get(1);
								String autospeed = lintrfc.get(2);
								if (!IUtils.isNullOrEmpty(duplex)) {
									if ("half".equals(duplex.toLowerCase())) {
										IZTUtils.addInJson(eth, "operationalDuplex", "half");
									} else if ("full".equals(duplex.toLowerCase())) {
										IZTUtils.addInJson(eth, "operationalDuplex", "full");
									} else if ("auto".equals(autospeed.toLowerCase())) {
										IZTUtils.addInJson(eth, "autoDuplex", true);
									}
								}
								if (!IUtils.isNullOrEmpty(duplex) && "auto".equals(duplex.toLowerCase())) {
									IZTUtils.addInJson(eth, "autoDuplex", false);
								}
							}
							// add in root json
							IZTUtils.addInJson(intrfc, "interfaceEthernet", eth);
						}
					}
				}
				if (!IUtils.isNull(intrfc)) {
					ifs.put(intrfc);
				}
			}
		}
		if (!IUtils.isNull(ifs)) {
			IZTUtils.addInJson(jifs, "cisco:interface", ifs);
		}
		return jifs;
	}

	/**
	 * Method to parse interface frame relay
	 * @param blob
	 * @return
	 */
	private static JSONObject parseFrameRelay(String blob) {
		JSONObject frame = new JSONObject();
		Map<String, String> map = IUtils.getRestParamMap(
				"encapsulation", "\\s+encapsulation\\s+frame-relay\\s+(\\S+)\\s*$",
				"lmiType", "^\\s+frame-relay\\s+lmi-type\\s+(\\S+)\\s*$");
		parseAndFillJson(blob, frame, map,	
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		JSONObject json = new JSONObject();
		IZTUtils.addInJson(json, "dlci", IRegexUtils.getFirstGrop(blob,
				"^\\s+frame-relay\\s+interface-dlci\\s+(\\d+)"));
		IZTUtils.addInJson(frame, "virtualFrameRelay", json);
		return frame;
	}

	/**
	 * Method to parse interface ipx
	 * @param blob
	 * @return
	 */
	private static JSONObject parseIPX(String blob) {
		JSONObject ipx = new JSONObject();
		Map<String, String> map = IUtils.getRestParamMap(
				"linkDelay", "^\\s+ipx\\s+link-delay\\s+(\\d+)",
				"throughPut", "^\\s+ipx\\s+throughput\\s+(\\d+)");
		parseAndFillJson(blob, ipx, map,
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		JSONObject json = new JSONObject();
		List<String> list = IRegexUtils.getMatchingGrops(
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, blob,
				"^\\s+ipx\\s+network\\s+(\\S+)\\s+encapsulation\\s+(\\S+)\\s+secondary");
		if (!IUtils.isNull(list) && list.size() >= 3) {
			IZTUtils.addInJson(json, "ipxAddress", list.get(1));
			IZTUtils.addInJson(json, "encapsulation", list.get(2));
			IZTUtils.addInJson(json, "ipxOrder", 2);
		} else {
			list = IRegexUtils.getMatchingGrops(
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, blob,
					"^\\s+ipx\\s+network\\s+(\\S+)\\s+encapsulation\\s+(\\S+)");
			if (!IUtils.isNull(list) && list.size() >= 3) {
				IZTUtils.addInJson(json, "ipxAddress", list.get(1));
				IZTUtils.addInJson(json, "encapsulation", list.get(2));
				IZTUtils.addInJson(json, "ipxOrder", 1);
			}
		}
		String ip = IRegexUtils.getFirstGrop(blob,
				"^\\s+ipx\\s+network\\s+(\\S+)\\s+secondary",
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		if (!IUtils.isNullOrEmpty(ip)) {
			IZTUtils.addInJson(json, "ipxAddress", ip);
			IZTUtils.addInJson(json, "ipxOrder", 2);
		} else {
			ip = IRegexUtils.getFirstGrop(blob,
					"^\\s+ipx\\s+network\\s+(\\S+)",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (!IUtils.isNullOrEmpty(ip)) {
				IZTUtils.addInJson(json, "ipxAddress", ip);
				IZTUtils.addInJson(json, "ipxOrder", 1);
			}
		}
		IZTUtils.addInJson(ipx, "interfaceIPXAddress", json);
		return ipx;
	}

	/**
	 * Method to parse interface ips
	 * @param blob
	 * @return
	 */
	private static JSONObject parseIP(String blob) {
		JSONObject json = new JSONObject();
		JSONArray arr = getIpConfigs(blob);
		if (!IUtils.isNull(arr) && arr.length()> 0) {
			IZTUtils.addInJson(json, "ipConfiguration", arr);
		}
		List<List<String>> ulst = IRegexUtils.getGlobalMultiMatchGrops(blob,
				"^\\s*ip address\\s+(\\S+)\\s+(\\S+)(\\ssecondary)", Pattern.MULTILINE);
		if (!IUtils.isNull(ulst) && ulst.size() > 0) {
			arr = new JSONArray();
			for (List<String> l : ulst) {
				if (!IUtils.isNull(l) && l.size() >= 2) {
					arr.put(l.get(1));
				}
			}
			if (!IUtils.isNull(arr) && arr.length()> 0) {
				IZTUtils.addInJson(json, "udpForwarder", arr);
			}
		}
		List<String> lset = IRegexUtils.getMatchingGrops(
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, blob,
				"^\\s+(no\\s+ip|ip)\\s+(directed.broadcast|local.proxy.arp|redirects|route.cache|mroute.cache)\\s*$");
		if (!IUtils.isNull(lset) && lset.size() >= 3) {
			String checkneg = lset.get(1);
			String ipsetting = lset.get(2);
			boolean bool = true;
			if (!IUtils.isNullOrEmpty(checkneg) &&
					"no ip".equals(checkneg)) {
				bool = false;
			}
			if (!IUtils.isNullOrEmpty(ipsetting)) {
				if (IRegexUtils.isMatch(ipsetting, "^directed.broadcast$")) {
					IZTUtils.addInJson(json, "directedBroadcast", bool);
				}
				if (IRegexUtils.isMatch(ipsetting, "^local.proxy.arp$")) {
					IZTUtils.addInJson(json, "localProxyARP", bool);
				}
				if (IRegexUtils.isMatch(ipsetting, "^redirects$")) {
					IZTUtils.addInJson(json, "redirects", bool);
				}
				if (IRegexUtils.isMatch(ipsetting, "^route.cache$")) {
					IZTUtils.addInJson(json, "routeCache", bool);
				}
				if (IRegexUtils.isMatch(ipsetting, "^mroute.cache$")) {
					IZTUtils.addInJson(json, "mRouteCache", bool);
				}
			}
		}
		return json;
	}

	/**
	 * Parse interface Serial
	 * @param blob
	 * @return
	 */
	private static JSONObject parseSerial(String blob) {
		JSONObject serial = new JSONObject();
		Map<String, String> map = IUtils.getRestParamMap(
				"dsuBandwidth", "^\\s+dsu.?bandwidth\\s+(\\d+)",
				"keepAlive", "^\\s+keepalive\\s+(\\d+)",
				"cableLength", "^\\s+cable.?length\\s+(\\d+)",
				"encapsulation", "^\\s+encapsulation\\s+(\\S+)",
				"idleCharacter", "^\\s+idle.?character\\s+(\\S+)",
				"framing", "^\\s+framing\\s+(\\S+)",
				"scramble", "^\\s+scramble\\s+(\\S+)",
				"dsuMode", "^\\s+dsu.?mode\\s+(\\S+)",
				"crc", "^\\s+crc\\s+(\\d+)");
		parseAndFillJson(blob, serial, map,
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		if (IRegexUtils.isMatch(blob, "^\\s+down-when-looped")) {
			IZTUtils.addInJson(serial, "downWhenLooped", true);
		} else {
			IZTUtils.addInJson(serial, "downWhenLooped", false);	
		}
		return serial;
	}

	/**
	 * Parse interface sonet
	 * @param blob
	 * @return
	 */
	private static JSONObject parseSonet(String blob) {
		JSONObject sonet = new JSONObject();
		Map<String, String> map = IUtils.getRestParamMap(
				"bandwidth", "^\\s+bandwidth\\s+(\\d+)",
				"clockSource", "^\\s+clock\\s*source\\s+(\\S+)",
				"crc", "^\\s+crc\\s+(\\d+)",
				"encapsulation", "^\\s+encapsulation\\s+(\\S+)",
				"keepAlive", "^\\s+keepalive\\s+(\\d+)",
				"framing", "^\\s+framing\\s+(\\S+)",
				"flag", "^\\s+flag[s]*\\s+(.+)",
				"loopBack", "^\\s+loopback\\s+(\\S+)");
		parseAndFillJson(blob, sonet, map,
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		if (IRegexUtils.isMatch(blob, "^\\s+down-when-looped")) {
			IZTUtils.addInJson(sonet, "downWhenLooped", true);
		} else {
			IZTUtils.addInJson(sonet, "downWhenLooped", false);	
		}
		return sonet;
	}

	/**
	 * Parse interface ATM
	 * @param blob
	 * @return
	 */
	private static JSONObject parseAtm(String blob) {
		JSONObject atm = new JSONObject();
		Map<String, String> map = IUtils.getRestParamMap(
				"loopback", "^\\s+loopback\\s+(\\S+)",
				"keepAlive", "^\\s+atm\\s+\\S+?keepalive\\s+(\\d+)",
				"clock", "^\\s+atm\\s+clock\\s+(.+)",
				"ilmiPVCDiscovery", "^\\s+atm\\s+\\S+?pvc-discovery\\s+(.+)",
				"scrambling", "^\\s+atm\\s+scrambling\\s+(.+)");
		parseAndFillJson(blob, atm, map,
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		List<List<String>> lAtms = IRegexUtils.getGlobalMultiMatchGrops(blob,
				"^(\\s*atm\\s+pvc\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+).*)$",
				Pattern.MULTILINE);
		if (!IUtils.isNull(lAtms)) {
			JSONArray arrAtm = new JSONArray();
			for (List<String> latm : lAtms) {
				if (!IUtils.isNull(latm) && latm.size() >= 6) {
					String line = latm.get(1);
					String encp = latm.get(5);
					JSONObject jobj = new JSONObject();
					IZTUtils.addInJson(jobj, "vpi", latm.get(3));
					IZTUtils.addInJson(jobj, "vci", latm.get(4));
					IZTUtils.addInJson(jobj, "vcd", latm.get(2));
					IZTUtils.addInJson(jobj, "encapsulation", encp);
					latm = IRegexUtils.getMatchingGrops(line,
							encp + "\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
					if (!IUtils.isNull(latm) && latm.size() >= 4) {
						IZTUtils.addInJson(jobj, "peak", latm.get(1));
						IZTUtils.addInJson(jobj, "average", latm.get(2));
						IZTUtils.addInJson(jobj, "burst", latm.get(3));
					} else {
						latm = IRegexUtils.getMatchingGrops(line,
								encp + "\\s+(\\d+)\\s+(\\d+)");
						if (!IUtils.isNull(latm) && latm.size() >= 3) {
							IZTUtils.addInJson(jobj, "peak", latm.get(1));
							IZTUtils.addInJson(jobj, "average", latm.get(2));
						}
					}
					String ln = IRegexUtils.getFirstGrop(line, "oam\\s+(\\d+)");
					if (!IUtils.isNullOrEmpty(ln)) {
						IZTUtils.addInJson(jobj, "oam", ln);
					}
					arrAtm.put(jobj);
				}
			}
			IZTUtils.addInJson(atm, "interfaceLogicalATM", arrAtm);
		}
		return atm;
	}

	/**
	 * Parse interface ISDN Bri
	 * @param blob
	 * @return
	 */
	private static JSONObject parseIsdnBri(String blob) {
		JSONObject isdn = new JSONObject();
		Map<String, String> map = IUtils.getRestParamMap(
				"encapsulation", "^\\s+encapsulation\\s+(\\S+)",
				"keepAlive", "^\\s+keepalive\\s+(\\d+)",
				"switchType", "^\\s+isdn\\s+switch.?type\\s+(\\S+)",
				"spid1", "^\\s+isdn\\s+spid1\\s+(.+)",
				"spid2", "^\\s+isdn\\s+spid2\\s+(.+)",
				"caller", "^\\s+isdn caller\\s+(\\S+)",
				"loopback", "^\\s+loopback\\s+(\\S+)");
		parseAndFillJson(blob, isdn, map,
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		return isdn;
	}

	/**
	 * Method to parse interface PPP
	 * @param blob
	 * @return
	 */
	private static JSONObject parsePPP(String blob) {
		JSONObject jppp = new JSONObject();
		Map<String, String> ppp = IUtils.getRestParamMap(
				"authenticationType", "^\\s+ppp\\s+authentication\\s+(\\S+)",
				"username", "^\\s+ppp\\s+\\S+\\s+sent-username\\s+(\\S+)",
				"callBack", "^\\s+ppp\\s+callback\\s+(\\S+)",
				"compression", "^\\s+ppp\\s+compression\\s+(\\S+)",
				"password", "^\\s+password\\s+(\\S+)");
		parseAndFillJson(blob, jppp, ppp,
				Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
		return jppp;
	}

	/**
	 * Method to parse and set first match values into json.
	 * @param blob 
	 * @param json
	 * @param map
	 * @param flags 
	 */
	private static void parseAndFillJson(String blob,
			JSONObject json, Map<String, String> map, int flags) {
		if (!IUtils.isNull(map)) {
			for (Entry<String, String> entry : map.entrySet()) {
				if (!IUtils.isNull(entry) && !IUtils.isNullOrEmpty(entry.getKey())
						 && !IUtils.isNullOrEmpty(entry.getValue())) {
					String val = IRegexUtils.getFirstGrop(blob, entry.getValue(), flags);
					if (!IUtils.isNullOrEmpty(val)) {
						IZTUtils.addInJson(json, entry.getKey(), val);
					}
				}
			}
		}
	}

	/**
	 * Method to parse ethernet object as json.
	 * @param blob
	 * @return
	 */
	private static JSONObject parseEthernet(String blob) {
		JSONObject eth = null;
		if (!IUtils.isNullOrEmpty(blob)) {
			eth = new JSONObject();
			List<String> list = IRegexUtils.getMatchingGrops(
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, blob,
					"^(\\s+(half|full|auto).duplex|\\s+duplex\\s+(\\S+))");
			String duplex = null;
			if (!IUtils.isNull(list) && list.size() >= 4) {
				if (!IUtils.isNullOrEmpty(list.get(2))) {
					duplex = list.get(2);
				} else if (!IUtils.isNullOrEmpty(list.get(3))) {
					duplex = list.get(3);
				}
			}
			if (!IUtils.isNullOrEmpty(duplex)) {
				if (duplex.contains("auto")) {
					IZTUtils.addInJson(eth, "autoDuplex", true);
				} else {
					IZTUtils.addInJson(eth, "autoDuplex", false);
					IZTUtils.addInJson(eth, "operationalDuplex", duplex);
				}
			} else if (IRegexUtils.isMatch(blob, "^\\s+speed\\s+\\d+",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE)) {
				IZTUtils.addInJson(eth, "autoSpeed", false);
			} else if (IRegexUtils.isMatch(blob, "^\\s+speed\\s+auto",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE)) {
				IZTUtils.addInJson(eth, "autoSpeed", true);
			} else if (IRegexUtils.isMatch(blob, "^\\s+no negotiation",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE)) {
				IZTUtils.addInJson(eth, "autoSpeed", false);
			}
		}
		return eth;
	}

	/**
	 * Method to process all ip configs
	 * @param blob
	 * @return
	 */
	private static JSONArray getIpConfigs(String blob) {
		JSONArray arr = null;
		if (!IUtils.isNull(blob)) {
			arr = new JSONArray();
			List<String> list = IRegexUtils.getMatchingGrops(
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE, blob,
					"^\\s*ip address\\s+(\\S+)\\s+(\\S+)\\s*$");
			int order = 1;
			order = getConfObj(arr, list, order, true);
			List<List<String>> lst = IRegexUtils.getGlobalMultiMatchGrops(blob,
					"^\\s*ip address\\s+(\\S+)\\s+(\\S+)(\\ssecondary)", Pattern.MULTILINE);
			if (!IUtils.isNull(lst)) {
				for (List<String> l : lst) {
					order = getConfObj(arr, l, order, true);
				}
			}
			order = 1;
			lst = IRegexUtils.getGlobalMultiMatchGrops(blob,
					"^\\s*ipv6 address\\s+([A-Z\\d:]+)\\/(\\d+)", Pattern.MULTILINE);
			if (!IUtils.isNull(lst)) {
				for (List<String> l : lst) {
					order = getConfObj(arr, l, order, true);
				}
			}
		}
		return arr;
	}

	/**
	 * Creates config json object
	 * @param arr
	 * @param list
	 * @param order
	 * @param mask
	 * @return
	 */
	private static int getConfObj(JSONArray arr, List<String> list, int order, boolean mask) {
		if (!IUtils.isNull(list) && list.size() >= 3) {
			JSONObject obj = new JSONObject();
			IZTUtils.addInJson(obj, "ipAddress", list.get(1));
			String val = list.get(2);
			if (mask) {
				val = IZTUtils.maskToBit(val);
			}
			IZTUtils.addInJson(obj, "mask", val);
			IZTUtils.addInJson(obj, "precedence", order);
			arr.put(obj);
			order++;
		}
		return order;
	}

	/**
	 * Check if device is physical
	 * @param in
	 * @return
	 */
	public static boolean isPhysical(String in) {
		if (!IUtils.isNullOrEmpty(in)) {
			if (in.toLowerCase().matches("seri|eth|gig|^fe|^fa|token|[bp]ri")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to parse local account config.
	 * @param runConfig
	 * @return
	 */
	public static JSONArray parseLocalAccounts(String runConfig) {
		JSONArray lcac = null;
		if (!IUtils.isNullOrEmpty(runConfig)) {
			lcac = new JSONArray();
			List<List<String>> lists = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"^username\\s+([^\\s]+)\\s+(.+)", Pattern.MULTILINE);
			if (!IUtils.isNull(lists)) {
				for (List<String> list : lists) {
					if (!IUtils.isNull(list) && list.size() >= 3) {
						JSONObject ac = new JSONObject();
						IZTUtils.addInJson(ac, "accountName", list.get(1));
						String userconfig = list.get(2);
						if (!IUtils.isNullOrEmpty(userconfig)) {
							String match = IRegexUtils.getFirstGrop(userconfig,
									"privilege\\s+([^\\s]+)");
							if (!IUtils.isNullOrEmpty(match)) {
								IZTUtils.addInJson(ac, "accessLevel", match);
							}
							match = IRegexUtils.getFirstGrop(userconfig,
									"password\\s+([^\\s]+)\\s+([^\\s]+)");
							if (!IUtils.isNullOrEmpty(match)) {
								IZTUtils.addInJson(ac, "password", match);
							}
						}
						lcac.put(ac);
					}
				}
			}
			lists = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"^enable\\s+(\\S+)\\s+(level\\s)?(\\d*)\\s?([^\\s]+)", Pattern.MULTILINE);
			if (!IUtils.isNull(lists)) {
				for (List<String> list : lists) {
					if (!IUtils.isNull(list) && list.size() >= 5) {
						JSONObject ac = new JSONObject();
						String user = "enable";
						if (!IUtils.isNullOrEmpty(list.get(1)) &&
							"secret".equals(list.get(1))) {
							user = "enablesecret";
						}
						String encrypt = "0";
						if (!IUtils.isNullOrEmpty(list.get(3))) {
							encrypt = list.get(3);
						}
						IZTUtils.addInJson(ac, "accountName", user);
						IZTUtils.addInJson(ac, "password", list.get(4));
						IZTUtils.addInJson(ac, "accessLevel", 15);
						IZTUtils.addInJson(ac, "encryption", encrypt);
						lcac.put(ac);
					}
				}
			}
		}
		return lcac;
	}

	/**
	 * Method to prarse routing information
	 * @param runConfig
	 * @param protocol
	 * @return
	 */
	public static JSONObject parseRouting(String runConfig, String protocol) {
		JSONObject rout = null;
		if (!IUtils.isNullOrEmpty(runConfig)) {
			rout = new JSONObject();
			List<List<String>> lists = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"(?s)^router\\s+(eigrp|ospf|bgp)\\s+(\\d+)(.+?)^!", Pattern.MULTILINE);
			if (!IUtils.isNull(lists)) {
				for (List<String> list : lists) {
					JSONObject proto = new JSONObject();
					if (!IUtils.isNull(list) && list.size() >= 4) {
						String type = list.get(1);
						String id = list.get(2);
						String blob = list.get(3);
						JSONObject areas = new JSONObject();
						String ns = "";
						if (!IUtils.isNullOrEmpty(type) && "eigrp".equals(type)) {
							ns = "cisco:";
						}
						List<List<String>> redists = IRegexUtils.getGlobalMultiMatchGrops(blob,
								"^\\s*redistribute\\s*(\\S+)(\\s(\\d+))?", Pattern.MULTILINE);
						if (!IUtils.isNull(redists)) {
							JSONArray arr = new JSONArray();
							for (List<String> redist : redists) {
								if (!IUtils.isNull(redist) && redist.size() >= 2) {
									JSONObject rdist = new JSONObject();
									IZTUtils.addInJson(rdist, "targetProtocol", redist.get(1));
									if (redist.size() >= 4) {
										IZTUtils.addInJson(rdist, "processId", redist.get(3));
									}
									arr.put(rdist);
								}
							}
							if (arr.length() > 0) {
								IZTUtils.addInJson(proto, ns + "redistribution", arr);
							}
						}
						if (IRegexUtils.isMatch(blob.toLowerCase(), "synchronization")) {
							IZTUtils.addInJson(proto, ns + "synchronization", 
									!IRegexUtils.isMatch(blob.toLowerCase(), "no synch"));
						}
						if (IRegexUtils.isMatch(blob.toLowerCase(), "auto-summary")) {
							IZTUtils.addInJson(proto, ns + "autoSummarization", 
									!IRegexUtils.isMatch(blob.toLowerCase(), "no auto-sum"));
						}
						if ("ospf".equals(type)) {
							List<List<String>> lareas = IRegexUtils.getGlobalMultiMatchGrops(blob,
									"^\\s*area\\s(\\d+\\S*)\\s(nssa|stub)", Pattern.MULTILINE);
							if (!IUtils.isNull(lareas)) {
								for (List<String> larea : lareas) {
									if (!IUtils.isNull(larea) && larea.size() >= 3) {
										String nm = larea.get(1);
										if (IUtils.isNull(areas.optJSONObject(nm))) {
											String tp = larea.get(2);
											JSONObject area = new JSONObject();
											IZTUtils.addInJson(area, "areaId", nm);
											if (!IUtils.isNullOrEmpty(tp) && "stub".equals(tp)) {
												tp = "SA";
											}
											IZTUtils.addInJson(area, "areaType", tp);
											// put in areas
											IZTUtils.addInJson(areas, nm, area);
										}
									}
								}
							}
							lareas = IRegexUtils.getGlobalMultiMatchGrops(blob,
									"^\\s*network\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+).+?area\\s(\\d+\\S*)",
									Pattern.MULTILINE);
							if (!IUtils.isNull(lareas)) {
								for (List<String> larea : lareas) {
									if (!IUtils.isNull(larea) && larea.size() >= 4) {
										String nm = larea.get(3);
										JSONObject net = new JSONObject();
										IZTUtils.addInJson(net, "address", larea.get(1));
										IZTUtils.addInJson(net, "mask", larea.get(2));
										JSONObject area = null;
										if (IUtils.isNull(areas.optJSONObject(nm))) {
											area = new JSONObject();
											IZTUtils.addInJson(area, "areaId", nm);
											IZTUtils.addInJson(area, "areaType", "normal");
										} else {
											area = areas.optJSONObject(nm);
										}
										if (!IUtils.isNull(area)) {
											JSONArray arr = new JSONArray();
											if (area.has("network")) {
												arr = area.optJSONArray("network");
											}
											arr.put(net);
											IZTUtils.addInJson(area, "network", arr);
										}
										// put in areas
										IZTUtils.addInJson(areas, nm, area);
									}
								}
							}
						} else {
							List<List<String>> nets = IRegexUtils.getGlobalMultiMatchGrops(blob,
									"^\\s*network\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)?",
									Pattern.MULTILINE);
							if (!IUtils.isNull(nets)) {
								JSONArray arrnet = new JSONArray();
								for (List<String> net : nets) {
									if (!IUtils.isNull(net) && net.size() >= 2) {
										JSONObject json = new JSONObject();
										IZTUtils.addInJson(json, "address", net.get(1));
										if (net.size() >= 3) {
											IZTUtils.addInJson(json, "mask", net.get(2));
										}
										arrnet.put(json);
									}
								}
								if (arrnet.length() > 0) {
									IZTUtils.addInJson(proto, ns + "network", arrnet);
								}
							}
						}
						// add areas
						IZTUtils.addInJson(proto, "areas", areas);
						List<List<String>> ladds = IRegexUtils.getGlobalMultiMatchGrops(blob,
								"summary-address\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)",
								Pattern.MULTILINE);
						if (!IUtils.isNull(ladds)) {
							JSONArray arrAdds = new JSONArray();
							for (List<String> ladd : ladds) {
								if (!IUtils.isNull(ladd) && ladd.size() >= 3) {
									JSONObject add = new JSONObject();
									IZTUtils.addInJson(add, "address", ladd.get(1));
									IZTUtils.addInJson(add, "mask", ladd.get(2));
									// put in array
									arrAdds.put(add);
								}
							}
							if (arrAdds.length() > 0) {
								IZTUtils.addInJson(proto, ns + "summarizedAddress", arrAdds);
							}
						}
						boolean allife = false;
						if (IRegexUtils.isMatch(blob, "^\\s*passive-interface default")) {
							allife = true;
						}
						if ("ospf".equals(type)) {
							IZTUtils.addInJson(proto, "allInterfacesEnabled", allife);
						} else if ("eigrp".equals(type)) {
							IZTUtils.addInJson(proto, "cisco:passiveInterfaceDefault", "false");
						}
						List<List<String>> llists = IRegexUtils.getGlobalMultiMatchGrops(blob,
								"^\\s*no passive-interface (\\S+)", Pattern.MULTILINE);
						if (!IUtils.isNull(llists)) {
							JSONArray lei = new JSONArray();
							JSONArray lai = new JSONArray();
							for (List<String> llist : llists) {
								if (!IUtils.isNull(llist) && llist.size() >= 2) {
									String val = llist.get(1);
									if (!IUtils.isNullOrEmpty(val) && !"default".equals(val)) {
										if ("ospf".equals(type)) {
											lei.put(val);
										} else if ("eigrp".equals(type)) {
											lai.put(val);
										}
									}
								}
							}
							if (lei.length() > 0) {
								IZTUtils.addInJson(proto, "enabledInterface", lei);
							}
							if (lai.length() > 0) {
								IZTUtils.addInJson(proto, "cisco:activeInterface", lai);
							}
						}
						llists = IRegexUtils.getGlobalMultiMatchGrops(blob,
								"^\\s*passive-interface (\\S+)", Pattern.MULTILINE);
						if (!IUtils.isNull(llists)) {
							JSONArray lei = new JSONArray();
							JSONArray lai = new JSONArray();
							for (List<String> llist : llists) {
								if (!IUtils.isNull(llist) && llist.size() >= 2) {
									String val = llist.get(1);
									if (!IUtils.isNullOrEmpty(val) && !"default".equals(val)) {
										if ("ospf".equals(type)) {
											lei.put(val);
										} else if ("eigrp".equals(type)) {
											lai.put(val);
										}
									}
								}
							}
							if (lei.length() > 0) {
								IZTUtils.addInJson(proto, "disabledInterface", lei);
							}
							if (lai.length() > 0) {
								IZTUtils.addInJson(proto, "cisco:passiveInterface", lai);
							}
						}
						if ("ospf".equals(type)) {
							IZTUtils.addInJson(proto, "processId", id);
							String routid = IRegexUtils.getFirstGrop(blob,
									"^\\s*router-id\\s*(\\S+)", Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
							if (!IUtils.isNullOrEmpty(routid)) {
								IZTUtils.addInJson(proto, "routerId", routid);
							} else {
								routid = IRegexUtils.getFirstGrop(protocol,
										"^Routing Protocol is \"" + type + " " + id + "\"(.+?)(?=^\\S)",
										Pattern.MULTILINE);
								if (!IUtils.isNullOrEmpty(routid)) {
									IZTUtils.addInJson(proto, "routerId", IRegexUtils.getFirstGrop(routid,
											"^\\s*Router ID\\s+(\\S+)"));
								}
							}
						} else if (IRegexUtils.isMatch(type, "eigrp|bgp")) {
							IZTUtils.addInJson(proto, ns + "asNumber", id);
						}
						// get the OSPF router ID
						if ("bgp".equals(type)) {
							JSONObject peers = new JSONObject();
							JSONArray neighbour = new JSONArray();
							llists = IRegexUtils.getGlobalMultiMatchGrops(blob,
									"^\\s*neighbor\\s(\\S+)\\s+remote-as\\s+(\\d+)",
									Pattern.MULTILINE);
							if (!IUtils.isNull(llists)) {
								for (List<String> llist : llists) {
									if (!IUtils.isNull(llist) && llist.size() >= 3) {
										String def = llist.get(1);
										String as = llist.get(2);
										if (!IUtils.isNullOrEmpty(def)) {
											if (def.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
												JSONObject nb = new JSONObject();
												IZTUtils.addInJson(nb, "address", def);
												IZTUtils.addInJson(nb, "asNumber", as);
												// put in array
												neighbour.put(nb);
											} else {
												IZTUtils.addInJson(peers, def, as);
											}
										}
									}
								}
							}
							llists = IRegexUtils.getGlobalMultiMatchGrops(blob,
									"^\\s*neighbor\\s(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+peer-group\\s+(\\S+)",
									Pattern.MULTILINE);
							if (!IUtils.isNull(llists)) {
								for (List<String> llist : llists) {
									if (!IUtils.isNull(llist) && llist.size() >= 3) {
										String as = llist.get(2);
										String ad = llist.get(1);
										if (!IUtils.isNullOrEmpty(as)) {
											if (peers.has(as)) {
												JSONObject nb = new JSONObject();
												IZTUtils.addInJson(nb, "address", ad);
												IZTUtils.addInJson(nb, "asNumber", peers.opt(as));
												// put in array
												neighbour.put(nb);
											}
										}
									}
								}
							}
						}
						if ("eigrp".equals(type)) {
							type = "cisco:" + type;
						}
						// finally add proto into routers
						IZTUtils.addInJson(rout, type, proto);
					}
				}
			}
		}
		return rout;
	}

	/**
	 * Method to parse snmp configuration.
	 * @param runConfig
	 * @param snmp
	 * @return
	 */
	public static JSONObject parseSnmp(String runConfig, String snmp) {
		JSONObject json = new JSONObject();
		if (!IUtils.isNullOrEmpty(runConfig)) {
			List<List<String>> llists = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"^(snmp-server\\s+community\\s+([^\\s]+).*)$", Pattern.MULTILINE);
			if (!IUtils.isNull(llists)) {
				JSONArray arr = new JSONArray();
				for (List<String> llist : llists) {
					if (!IUtils.isNull(llist) && llist.size() >= 3) {
						JSONObject community = new JSONObject();
						String line = llist.get(1);
						IZTUtils.addInJson(community, "communityString", llist.get(2));
						String val = IRegexUtils.getFirstGrop(line, "\\s+(RO|RW)");
						if (!IUtils.isNullOrEmpty(val)) {
							IZTUtils.addInJson(community, "accessType", val);
						}
						val = IRegexUtils.getFirstGrop(line, "\\s+view\\s+([^\\s]+)");
						if (!IUtils.isNullOrEmpty(val)) {
							IZTUtils.addInJson(community, "mibView", val);
						}
						val = IRegexUtils.getMatchGrop(2, line,
								"\\s+(RO|RW)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
						if (!IUtils.isNullOrEmpty(val)) {
							IZTUtils.addInJson(community, "filter", val);
						}
						arr.put(community);
					}
				}
				IZTUtils.addInJson(json, "community", arr);
			}
			Map<String, String> map = IUtils.getRestParamMap(
					"sysContact","^snmp-server\\s+contact\\s+(.*[^\\s])\\s*$",
					"sysLocation","^snmp-server\\s+location\\s+(.*[^\\s])\\s*$",
					"sysName","^hostname\\s+(\\S+)",
					"trapSource", "^snmp-server\\s+trap-source\\s+(.*[^\\s])\\s*$",
					"trapTimeout", "^snmp-server\\s+trap-timeout\\s+(\\d+)");
			parseAndFillJson(runConfig, json, map,
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE);
			if (IRegexUtils.isMatch(runConfig,
					"^snmp-server\\s+system-shutdown\\s*$",
					Pattern.MULTILINE & Pattern.CASE_INSENSITIVE)) {
				IZTUtils.addInJson(json, "systemShutdownViaSNMP", true);
			}
			llists = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"^(snmp-server\\s+host\\s+\\\"?(\\d+\\.\\d+\\.\\d+\\.\\d+)\\\"?.*$)",
					Pattern.MULTILINE);
			if (!IUtils.isNull(llists)) {
				JSONArray arr = new JSONArray();
				for (List<String> llist : llists) {
					if (!IUtils.isNull(llist) && llist.size() >= 3) {
						JSONObject host = new JSONObject();
						String line = llist.get(1);
						IZTUtils.addInJson(host, "ipAddress", llist.get(2));
						line = IRegexUtils.getFirstGrop(line,
								"\\\"?\\d+\\.\\d+\\.\\d+\\.\\d+\\\"?\\s+([^\\s]+.*)$");
						if (!IUtils.isNullOrEmpty(line)) {
							String comstr = IRegexUtils.getFirstGrop(line,
									"^trap\\s+version\\s+\\S+\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
							if (IUtils.isNullOrEmpty(comstr)) {
								comstr = IRegexUtils.getFirstGrop(line,
										"^trap\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
								if (IUtils.isNullOrEmpty(comstr)) {
									comstr = IRegexUtils.getFirstGrop(line,
											"^inform\\s+version\\s+\\S+\\s+(\\S+)",
											Pattern.CASE_INSENSITIVE);
									if (IUtils.isNullOrEmpty(comstr)) {
										comstr = IRegexUtils.getFirstGrop(line,
												"^inform\\s+(\\S+)",
												Pattern.CASE_INSENSITIVE);
										if (IUtils.isNullOrEmpty(comstr)) {
											comstr = IRegexUtils.getFirstGrop(line,
													"^(\\S+)",
													Pattern.CASE_INSENSITIVE);
										}
									}
								}
							}
							if (!IUtils.isNullOrEmpty(comstr)) {
								IZTUtils.addInJson(host, "communityString", comstr);
							}
						}
						arr.put(host);
					}
				}
				IZTUtils.addInJson(json, "trapHosts", arr);
			}
		}
		return json;
	}

	/**
	 * Method to parse span tree configuration.
	 * @param stp
	 * @return
	 */
	public static JSONArray parseSTP(String stp) {
		JSONArray arr = null;
		if (!IUtils.isNullOrEmpty(stp)) {
			List<List<String>> llists = IRegexUtils.getGlobalMultiMatchGrops(stp,
					"(?s)^(VLAN|MST)0*(\\d+)(.+?)^-+", Pattern.MULTILINE);
			if (!IUtils.isNull(llists)) {
				arr = new JSONArray();
				for (List<String> llist : llists) {
					if (!IUtils.isNull(llist) && llist.size() >= 4) {
						String blob = llist.get(3);
						JSONObject inst = new JSONObject();
						IZTUtils.addInJson(inst, "vlan", llist.get(2));
						System.out.println("blob: " + blob);
						String root = IRegexUtils.getFirstGrop(blob,
								"(?s)^\\s*Root\\s+ID(.+?)^\\s*$", Pattern.MULTILINE);
						if (!IUtils.isNullOrEmpty(root)) {
							String mac = IRegexUtils.getFirstGrop(root, "Address\\s+([0-9a-f.]+)");
							if (!IUtils.isNullOrEmpty(mac)) {
								IZTUtils.addInJson(inst, "designatedRootMacAddress",
										IZTUtils.stripMac(mac));
							}
							Map<String, String> map = IUtils.getRestParamMap(
									"designatedRootPriority","Priority\\s+(\\d+)",
									"designatedRootCost","Cost\\s+(\\d+)",
									"designatedRootPort","Port\\s+\\d+\\s+\\((\\S+)\\)");
							parseAndFillJson(root, inst, map, Pattern.CASE_INSENSITIVE);
							List<String> lst = IRegexUtils.getMatchingGrops(root,
									"Hello Time\\s+(\\d+) sec\\s+Max Age\\s+(\\d+) sec\\s+Forward Delay\\s+(\\d+) sec");
							if (!IUtils.isNull(lst) && lst.size() >= 4) {
								IZTUtils.addInJson(inst, "designatedRootHelloTime", lst.get(1));
								IZTUtils.addInJson(inst, "designatedRootMaxAge", lst.get(2));
								IZTUtils.addInJson(inst, "designatedRootForwardDelay", lst.get(3));
							}
						}
						String bridge = IRegexUtils.getFirstGrop(blob,
								"(?s)^\\s*Root\\s+ID(.+?)^\\s*$", Pattern.MULTILINE);
						if (!IUtils.isNullOrEmpty(bridge)) {
							String mac = IRegexUtils.getFirstGrop(bridge, "Address\\s+([0-9a-f.]+)");
							if (!IUtils.isNullOrEmpty(mac)) {
								IZTUtils.addInJson(inst, "systemMacAddress",
										IZTUtils.stripMac(mac));
							}
							IZTUtils.addInJson(inst, "priority",
									IRegexUtils.getFirstGrop(bridge, "Priority\\s+(\\d+)"));
							List<String> lst = IRegexUtils.getMatchingGrops(bridge,
									"Hello Time\\s+(\\d+) sec\\s+Max Age\\s+(\\d+) sec\\s+Forward Delay\\s+(\\d+) sec");
							if (!IUtils.isNull(lst) && lst.size() >= 4) {
								IZTUtils.addInJson(inst, "helloTime", lst.get(1));
								IZTUtils.addInJson(inst, "maxAge", lst.get(2));
								IZTUtils.addInJson(inst, "forwardDelay", lst.get(3));
							}
						}
						arr.put(inst);
					}
				}
			}
		}
		return arr;
	}

	/**
	 * Method to parse static routes configuration.
	 * @param runConfig
	 * @return
	 */
	public static JSONArray parseStaticRoutes(String runConfig) {
		JSONArray arr = null;
		if (!IUtils.isNullOrEmpty(runConfig)) {
			List<List<String>> llists = IRegexUtils.getGlobalMultiMatchGrops(runConfig,
					"^ip route\\s+(\\d+\\.\\S+)\\s+(\\d+\\.\\S+)\\s+(\\S+)(.*)", Pattern.MULTILINE);
			if (!IUtils.isNull(llists)) {
				arr = new JSONArray();
				for (List<String> llist : llists) {
					if (!IUtils.isNull(llist) && llist.size() >= 4) {
						JSONObject routes = new JSONObject();
						String dadd = llist.get(1);
						String dmask = llist.get(2);
						String gateway = llist.get(3);
						String reminder = llist.get(4);
						IZTUtils.addInJson(routes, "destinationAddress", dadd);
						IZTUtils.addInJson(routes, "destinationMask", dmask);
						if (IRegexUtils.isMatch(gateway, "\\d+\\.\\d+\\.\\d+\\.\\d+")) {
							IZTUtils.addInJson(routes, "gatewayAddress", gateway);
						} else {
							IZTUtils.addInJson(routes, "interface", gateway);
						}
						if (!IUtils.isNullOrEmpty(dadd) && "0.0.0.0".equals(dadd) &&
								!IUtils.isNullOrEmpty(dmask) && "0.0.0.0".equals(dmask)) {
							IZTUtils.addInJson(routes, "defaultGateway", true);
						} else {
							IZTUtils.addInJson(routes, "defaultGateway", true);
						}
						if (!IUtils.isNullOrEmpty(reminder)) {
							String val = IRegexUtils.getFirstGrop(reminder, "^\\s*(\\d+)\\s*$");
							if (IUtils.isNullOrEmpty(val)) {
								val = IRegexUtils.getFirstGrop(reminder, "^\\s*(\\d+)\\s+.*$");
								if (IUtils.isNullOrEmpty(val)) {
									val = IRegexUtils.getFirstGrop(reminder, "^\\s*\\S+\\s+(\\d+)\\s+.*$");
								}
							}
							if (IUtils.isNullOrEmpty(val)) {
								IZTUtils.addInJson(routes, "routePreference", val);
							}
						}
						//put in arr
						arr.put(routes);
					}
				}
			}
			String run = IRegexUtils.getFirstGrop(runConfig,
					"^ip default-gateway\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*$",
					Pattern.MULTILINE);
			if (!IUtils.isNullOrEmpty(run)) {
				JSONObject routes = new JSONObject();
				IZTUtils.addInJson(routes, "destinationAddress", "0.0.0.0");
				IZTUtils.addInJson(routes, "destinationMask", "0.0.0.0");
				IZTUtils.addInJson(routes, "gatewayAddress", run);
				IZTUtils.addInJson(routes, "defaultGateway", true);
				//put in arr
				arr.put(routes);
			}
		}
		return arr;
	}

	/**
	 * Method to parse vlan configuration.
	 * @param vlans
	 * @return
	 */
	public static JSONArray parseVlans(String vlans) {
		JSONArray arr = null;
		if (!IUtils.isNullOrEmpty(vlans)) {
			String sec = IRegexUtils.getFirstGrop(vlans,
					"(?s)^VLAN\\s+Name\\s+Status\\s+Ports(.+?)^\\s*$",
					Pattern.MULTILINE);
			if (!IUtils.isNullOrEmpty(sec)) {
				List<List<String>> llists = IRegexUtils.getGlobalMultiMatchGrops(vlans,
						"(?s)^VLAN\\s+Type\\s+SAID\\s+MTU\\s+Parent\\s+RingNo\\s+BridgeNo\\s+Stp\\s+BrdgMode\\s+Trans1\\s+Trans2(.+?)^\\s*$",
						Pattern.MULTILINE);
				if (!IUtils.isNull(llists)) {
					String sec1 = sec + "\nEND";
					String sec2 = "";
					arr = new JSONArray();
					for (List<String> llist : llists) {
						if (!IUtils.isNull(llist) && llist.size() >= 2) {
							sec2 += llist.get(1);
						}
					}
					// process both sections
					llists = IRegexUtils.getGlobalMultiMatchGrops(sec1,
							"(?s)^(\\d+)\\s+(\\S+)\\s+(\\S+)(.*?)(?=^\\b)", Pattern.MULTILINE);
					if (!IUtils.isNull(llists)) {
						for (List<String> llist : llists) {
							if (!IUtils.isNull(llist) && llist.size() >= 4) {
								JSONObject json = new JSONObject();
								String num = llist.get(1);
								String status = llist.get(3);
								String ports = null;
								if (llist.size() >= 5) {
									ports = llist.get(4);
								}
								IZTUtils.addInJson(json, "number", num);
								IZTUtils.addInJson(json, "name", llist.get(2));
								if (!IUtils.isNullOrEmpty(status) && status.contains("act")) {
									IZTUtils.addInJson(json, "enabled", true);
								} else {
									IZTUtils.addInJson(json, "enabled", true);
								}
								if (!IUtils.isNullOrEmpty(ports)) {
									JSONArray prts = new JSONArray();
									for (String ln : ports.split("\n")) {
										for (String it : ln.split(",")) {
											prts.put(IZTUtils.getFullPort(it));
										}
									}
									if (prts.length() > 0) {
										IZTUtils.addInJson(json, "interfaceMember", prts);
									}
								}
								List<String> lst = IRegexUtils.getMatchingGrops(Pattern.MULTILINE, sec2,
										"^" + num + "+\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\S+\\s+(\\S+)\\s+(\\d+)\\s+(\\d+)");
								if (!IUtils.isNull(lst) && lst.size() >= 10) {
									IZTUtils.addInJson(json, "implementationType", lst.get(1));
									IZTUtils.addInJson(json, "said", lst.get(2));
									IZTUtils.addInJson(json, "mtu", lst.get(3));
									if (!"-".equals(lst.get(4))) IZTUtils.addInJson(json, "parent", lst.get(4));
									if (!"-".equals(lst.get(5))) IZTUtils.addInJson(json, "ringNumber", lst.get(5));
									if (!"-".equals(lst.get(6))) IZTUtils.addInJson(json, "bridgeNumber", lst.get(6));
									if (!"-".equals(lst.get(7))) IZTUtils.addInJson(json, "bridgeMode", lst.get(7));
									IZTUtils.addInJson(json, "translationBridge1", lst.get(8));
									IZTUtils.addInJson(json, "translationBridge2", lst.get(9));
								}
								// put in array
								arr.put(json);
							}
						}
					}
				}
			} else { // MSFC output
				List<List<String>> llists = IRegexUtils.getGlobalMultiMatchGrops(vlans,
						"(?s)Virtual LAN ID:\\s+(\\d+)\\s+(.+?)(?=^\\S)",
						Pattern.MULTILINE);
				if (!IUtils.isNull(llists) && llists.size() > 0) {
					arr = new JSONArray();
					for (List<String> list : llists) {
						if (!IUtils.isNull(list) && list.size() >= 3) {
							JSONObject json = new JSONObject();
							IZTUtils.addInJson(json, "number", list.get(1));
							IZTUtils.addInJson(json, "name", "vlan" + list.get(1));
							IZTUtils.addInJson(json, "enabled", true);
							String blob = list.get(2);
							if (!IUtils.isNullOrEmpty(blob)) {
								List<List<String>> llist = IRegexUtils.getGlobalMultiMatchGrops(blob,
										"VLAN Trunk Interface:\\s+(\\S+)", Pattern.MULTILINE);
								if (!IUtils.isNull(llist) && llist.size() > 0) {
									JSONArray ims = new JSONArray();
									for (List<String> lst : llist) {
										if (!IUtils.isNull(lst) && lst.size() >= 2) {
											ims.put(IZTUtils.getFullPort(lst.get(1)));
										}
									}
									IZTUtils.addInJson(json, "interfaceMember", ims);
								}
							}
							// add in array
							arr.put(json);
						}
					}
				}
			}
		}
		return arr;
	}

	/**
	 * Method to parse vtp status information
	 * @param vtpStatus
	 * @return
	 */
	public static JSONObject parseVtp(String vtpStatus) {
		JSONObject vtp = null;
		if (!IUtils.isNullOrEmpty(vtpStatus)) {
			vtp = new JSONObject();
			Map<String, String> map = IUtils.getRestParamMap(
					"cisco:version", "^VTP Version\\s*:\\s*(\\d+)$",
					"cisco:configVersion", "^Configuration Revision\\s*:\\s*(\\d+)$",
					"cisco:maxVlanCount", "^Maximum VLANs supported locally\\s*:\\s*(\\d+)$",
					"cisco:vlanCount", "^Number of existing VLANs\\s*:\\s*(\\d+)$",
					"cisco:localMode", "^VTP Operating Mode\\s*:\\s*(\\S+)$",
					"cisco:domainName", "^VTP Domain Name\\s*:\\s*(\\S+)$",
					"cisco:v2Mode", "^VTP V2 Mode\\s*:\\s*(\\S+)$",
					"cisco:password", "^MD5 digest\\s*:\\s*(.*[^\\s])$",
					"cisco:lastUpdater", "^last modified by\\s+(\\S+)");
			parseAndFillJson(vtpStatus, vtp, map, Pattern.MULTILINE);
			String val = IRegexUtils.getFirstGrop(vtpStatus,
					"^VTP Pruning Mode\\s*:\\s*(\\S+)$", Pattern.MULTILINE);
			if (!IUtils.isNullOrEmpty(val) && val.toLowerCase().contains("enabled")) {
				IZTUtils.addInJson(vtp, "cisco:vlanPruningEnabled", true);
			} else {
				IZTUtils.addInJson(vtp, "cisco:vlanPruningEnabled", false);
			}
			val = IRegexUtils.getFirstGrop(vtpStatus,
					"^VTP Traps Generation\\s*:\\s*(\\S+)$", Pattern.MULTILINE);
			if (!IUtils.isNullOrEmpty(val) && val.toLowerCase().contains("enabled")) {
				IZTUtils.addInJson(vtp, "cisco:alarmNotificationEnabled", true);
			} else {
				IZTUtils.addInJson(vtp, "cisco:alarmNotificationEnabled", false);
			}
			if (vtp.has("cisco:localMode")) {
				IZTUtils.addInJson(vtp, "cisco:serviceType", "vtp");
			}
		}
		return vtp;
	}
}
