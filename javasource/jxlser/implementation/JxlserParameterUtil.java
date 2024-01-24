package jxlser.implementation;

import java.util.HashMap;
import java.util.Map;

public class JxlserParameterUtil {
	static ThreadLocal<Map<String, Object>> nextParameters = new ThreadLocal<Map<String, Object>>();

	public static Map<String, Object> getNextParameters() {
		if (nextParameters.get() == null) {
			nextParameters.set(new HashMap<String, Object>());
		}
		return nextParameters.get();
	}

	public static void resetParameters() {
		nextParameters.set(new HashMap<String, Object>());;
	}

	public static void addParameter(String name, Object value) {
		getNextParameters().put(name, value);
	}
}
