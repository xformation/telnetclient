/**
 * 
 */
package com.synectiks.telnet.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.commons.utils.IUtils;

/**
 * @author Rajesh
 */
public interface IRegexUtils {

	Logger logger = LoggerFactory.getLogger(IRegexUtils.class);

	/**
	 * Method to check if <i>regex</i> is matches into <i>src</i>
	 * @param src
	 * @param regex
	 * @return
	 */
	static boolean isMatch(String src, String regex) {
		if (!IUtils.isNullOrEmpty(src) && !IUtils.isNullOrEmpty(regex)) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(src);
			return m.find();
		}
		return false;
	}

	/**
	 * Method to check if <i>regex</i> is matches into <i>src</i>
	 * @param src
	 * @param regex
	 * @param flags Match flags, a bit mask that may include
	 * 		CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ,
	 * 		UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and COMMENTS
	 * @return
	 */
	static boolean isMatch(String src, String regex, int flags) {
		if (!IUtils.isNullOrEmpty(src) && !IUtils.isNullOrEmpty(regex)) {
			Pattern p = Pattern.compile(regex, flags);
			Matcher m = p.matcher(src);
			return m.find();
		}
		return false;
	}

	/**
	 * Method to evaluate <i>regex</i> and returns matching groups in <i>src</i>
	 * @param src
	 * @param regex
	 * @return
	 */
	static String getFirstGrop(String src, String regex) {
		List<String> groups = getMatchingGrops(src, regex);
		if (!IUtils.isNull(groups) && groups.size() > 0) {
			if (groups.size() >= 2) {
				logger.warn("There is more matching groups in list: " + groups);
			}
			return groups.get(1);
			 
		}
		return null;
	}

	/**
	 * Method to get specified matching group from string.
	 * @param grp
	 * @param src
	 * @param regex
	 * @param flags Match flags, a bit mask that may include
	 * 		CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ,
	 * 		UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and COMMENTS
	 * 		or -1 for default
	 * @return
	 */
	static String getMatchGrop(int grp, String src, String regex, int flags) {
		List<String> groups = null;
		if (flags > -1) {
			groups = getMatchingGrops(src, regex);
		} else {
			groups = getMatchingGrops(flags, src, regex);
		}
		if (!IUtils.isNull(groups) && groups.size() > 0) {
			if (groups.size() >= (grp + 1)) {
				return groups.get(grp);
			} else {
				logger.warn("There is NO matching groups in list: " + groups);
			}
		}
		return null;
	}

	/**
	 * Method to evaluate <i>regex</i> and returns matching groups in <i>src</i>
	 * @param src
	 * @param regex
	 * @param flags Match flags, a bit mask that may include
	 * 		CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ,
	 * 		UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and COMMENTS
	 * @return
	 */
	static String getFirstGrop(String src, String regex, int flags) {
		List<String> groups = getMatchingGrops(flags, src, regex);
		if (!IUtils.isNull(groups) && groups.size() > 0) {
			if (groups.size() >= 2) {
				logger.warn("There is more matching groups in list: " + groups);
			}
			return groups.get(1);
			 
		}
		return null;
	}

	/**
	 * Method to evaluate <i>regex</i> and find matching groups in <i>src</i>
	 * @param flags Match flags, a bit mask that may include
	 * 		CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ,
	 * 		UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and COMMENTS
	 * @param src
	 * @param regex
	 * @param grps group counts starts from 1
	 * @return List of matching strings [First group at 0 is whole string so skip it]
	 */
	static List<String> getMatchingGrops(int flags, String src, String regex, int... grps) {
		List<String> matches = null;
		if (!IUtils.isNullOrEmpty(src) && !IUtils.isNullOrEmpty(regex)) {
			Pattern p = Pattern.compile(regex, flags);
			Matcher m = p.matcher(src);
			matches = new ArrayList<>();
			if (m.find()) {
				for(int i = 0; i <= m.groupCount(); i++) {
					matches.add(m.group(i));
				}
			} else {
				logger.warn("No match found for regex '{}'", regex);
				return null;
			}
		} else {
			logger.error("source '{}' and regex '{}' should not be null or empty.",
					src, regex);
		}
		if (!IUtils.isNull(matches) && grps.length > 0) {
			List<String> res = new ArrayList<>();
			logger.info("{} matches for {} groups", matches.size(), grps.length);
			for (int grp : grps) {
				if (grp > 0 && grp < matches.size()) {
					res.add(matches.get(grp));
				}
			}
			logger.info("Res: " + res);
			return res;
		}
		logger.info("Matches: " + matches);
		return matches;
	}

	/**
	 * Method to evaluate <i>regex</i> and find matching groups in <i>src</i>
	 * @param src
	 * @param regex
	 * @param grps group counts starts from 1
	 * @return List of matching strings [First group at 0 is whole string so skip it]
	 */
	static List<String> getMatchingGrops(String src, String regex, int... grps) {
		List<String> matches = null;
		if (!IUtils.isNullOrEmpty(src) && !IUtils.isNullOrEmpty(regex)) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(src);
			matches = new ArrayList<>();
			if (m.find()) {
				for(int i = 0; i <= m.groupCount(); i++) {
					matches.add(m.group(i));
				}
			} else {
				logger.warn("No match found for regex '{}'", regex);
				return null;
			}
		} else {
			logger.error("source '{}' and regex '{}' should not be null or empty.",
					src, regex);
		}
		if (!IUtils.isNull(matches) && grps.length > 0) {
			List<String> res = new ArrayList<>();
			logger.info("{} matches for {} groups", matches.size(), grps.length);
			for (int grp : grps) {
				if (grp > 0 && grp < matches.size()) {
					res.add(matches.get(grp));
				}
			}
			logger.info("Res: " + res);
			return res;
		}
		logger.info("Matches: " + matches);
		return matches;
	}

	/**
	 * Method to evaluate <i>regex</i> and find multiple matching groups in <i>src</i>
	 * @param src
	 * @param regex
	 * @param flags Match flags, a bit mask that may include
	 * 		CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ,
	 * 		UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and COMMENTS
	 * @return List of matching strings [First group at 0 is whole string so skip it]
	 */
	static List<List<String>> getGlobalMultiMatchGrops(
			String src, String regex, int flags) {
		List<List<String>> matches = null;
		if (!IUtils.isNullOrEmpty(src) && !IUtils.isNullOrEmpty(regex)) {
			Pattern p = Pattern.compile(regex, flags);
			Matcher m = p.matcher(src);
			matches = new ArrayList<>();
			while (m.find()) {
				List<String> match = new ArrayList<>();
				for(int i = 0; i <= m.groupCount(); i++) {
					match.add(m.group(i));
				}
				if (!IUtils.isNull(match) && !match.isEmpty()) {
					matches.add(match);
				}
			}
			if (matches.isEmpty()) {
				logger.warn("No match found for regex '{}'", regex);
				return null;
			}
		} else {
			logger.error("source '{}' and regex '{}' should not be null or empty.",
					src, regex);
		}
		logger.info("Matches: " + matches);
		return matches;
	}

	/**
	 * Method to parse the list date string into time in milliseconds.
	 * @param restart
	 * @return
	 */
	static long getTimeFromEpoch(List<String> restart) {
		if (!IUtils.isNull(restart) && !restart.isEmpty() && restart.size() >= 5) {
			String time = restart.get(1);
			String timezone = restart.get(2);
			String month = restart.get(3);
			String day = restart.get(4);
			String year = restart.get(5);
			String format = "MMM dd, yyyy HH:mm:ss z";
			String formt = "MMM d, yyyy HH:mm:ss z";
			Date date = null;
			try {
				date = DateUtils.parseDate(month + " " + day + ", " + year
						+ " " + time + " " + timezone, format, formt);
			} catch (ParseException e) {
				logger.error(e.getMessage());
			}
			if (!IUtils.isNull(date)) {
				return date.getTime();
			}
		}
		return 0;
	}

	/**
	 * Extract uptime string to calculate time in millis from epoch. 
	 * @param uptime
	 * @return
	 */
	static long getUptimeFromEpoch(String uptime) {
		if (!IUtils.isNullOrEmpty(uptime)) {
			long time = Calendar.getInstance().getTimeInMillis();
			time -= (extractInt(uptime, "(\\d+)\\s*years?") * 52 * 7 * 24 * 60 * 60);
			time -= (extractInt(uptime, "(\\d+)\\s*weeks?") * 7 * 24 * 60 * 60);
			time -= (extractInt(uptime, "(\\d+)\\s*days?") * 24 * 60 * 60);
			time -= (extractInt(uptime, "(\\d+)\\s*hours?") * 60 * 60);
			time -= (extractInt(uptime, "(\\d+)\\s*minutes?") * 60);
			return time;
		}
		return 0;
	}

	/**
	 * Method to evaluate the regex and return an integer number if found.
	 * @param src
	 * @param regex
	 * @return
	 */
	static int extractInt(String src, String regex) {
		if (!IUtils.isNullOrEmpty(src) && !IUtils.isNullOrEmpty(regex)) {
			String res = getFirstGrop(src, regex);
			return extractInt(res);
		}
		return 0;
	}

	/**
	 * Method to convert string into integer number
	 * @param res
	 * @return
	 */
	static int extractInt(String res) {
		if (!IUtils.isNullOrEmpty(res) && res.matches("\\d+")) {
			try {
				return Integer.parseInt(res);
			} catch (NumberFormatException nfe) {
				// Ignore it.
			}
		}
		return 0;
	}
}
