/**
 * 
 */
package com.synectiks.telnet.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synectiks.commons.exceptions.SynectiksException;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.telnet.interfaces.IZipTieAdapter;

/**
 * @author Rajesh
 */
public class AdapterFactory {

	private static final Logger logger = LoggerFactory.getLogger(AdapterFactory.class);

	private static AdapterFactory instance = null;

	/** private constructor */
	private AdapterFactory() {
		
	}

	/**
	 * Method to load an adapter if match found
	 * @param cls
	 * @return
	 * @throws SynectiksException
	 */
	public IZipTieAdapter getAdapter(String cls) throws Throwable {
		if (!IUtils.isNullOrEmpty(cls)) {
			cls = cls.trim().replaceAll(":", "");
			Class<?> inst = Class.forName("com.synectiks.telnet.adapters." + cls);
			logger.info("Found an Adapter class: " + inst.getName());
			return (IZipTieAdapter) inst.newInstance();
		} else {
			throw new SynectiksException("Adapter 'cls' is null or empty");
		}
	}

	/**
	 * Method returns the static instance of <code>AdapterFactory</code>
	 * @return
	 */
	public static AdapterFactory getInstance() {
		if (IUtils.isNull(instance)) {
			instance = new AdapterFactory();
		}
		return instance;
	}

}
