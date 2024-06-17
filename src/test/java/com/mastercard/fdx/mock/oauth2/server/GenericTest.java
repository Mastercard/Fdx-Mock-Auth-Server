package com.mastercard.fdx.mock.oauth2.server;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class GenericTest { // NOSONAR

	public <T> void testGenericGetterSetter(List<T> acts) throws Exception {
		for (T act : acts) {
			testGenericGetterSetter(act);
		}
	}

	public <T> void testGenericGetterSetter(T act) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, MalformedURLException, ClassNotFoundException, InstantiationException {
		Method[] methods = act.getClass().getMethods();

		/* Set Data */
		for (int index = 0; index < methods.length; index++) {
			if (methods[index].getName().startsWith("set")) {
				System.err.println(methods[index].getParameterTypes()[0]);
				if (methods[index].getParameterTypes()[0] == String.class) {
					methods[index].invoke(act, "123");
				}else if (methods[index].getParameterTypes()[0] == String[].class) {
					String[] args = {"one", "two"};
					methods[index].invoke(act, new Object[]{args});
				}else if (methods[index].getParameterTypes()[0] == byte[].class) {
					byte[] myvar = "This is a Test String".getBytes();
					methods[index].invoke(act, myvar);
				} else if (methods[index].getParameterTypes()[0] == Calendar.class) {
					methods[index].invoke(act, Calendar.getInstance());
				} else if (methods[index].getParameterTypes()[0] == Date.class) {
					methods[index].invoke(act, new Date());
				} else if (methods[index].getParameterTypes()[0] == Map.class) {
					Map<Object, Object> detail = new HashMap<Object, Object>();
					detail.put(new Object(), new Object());
					methods[index].invoke(act, detail);
				} else if (methods[index].getParameterTypes()[0] == Long.class
						|| methods[index].getParameterTypes()[0] == long.class) {
					methods[index].invoke(act, 123L);
				} else if (methods[index].getParameterTypes()[0] == Character.class
						|| methods[index].getParameterTypes()[0] == char.class) {
					methods[index].invoke(act, 'A');
				} else if (methods[index].getParameterTypes()[0] == Double.class
						|| methods[index].getParameterTypes()[0] == double.class) {
					methods[index].invoke(act, 123D);
				} else if (methods[index].getParameterTypes()[0] == List.class) {
					List<Object> objectList = new ArrayList<Object>();
					methods[index].invoke(act, objectList);
				} else if (methods[index].getParameterTypes()[0] == Set.class) {
					Set<Object> objectSet = new HashSet<>();
					methods[index].invoke(act, objectSet);
				}else if (methods[index].getParameterTypes()[0] == boolean.class
						|| methods[index].getParameterTypes()[0] == Boolean.class) {
					methods[index].invoke(act, true);
				} else if (methods[index].getParameterTypes()[0] == Integer.class
						|| methods[index].getParameterTypes()[0] == int.class) {
					methods[index].invoke(act, 123);
				} else if (methods[index].getParameterTypes()[0] == URL.class) {
					methods[index].invoke(act, new URL("https://test.com"));
				} else if (methods[index].getParameterTypes()[0].getName().endsWith("Enum")
						|| methods[index].getParameterTypes()[0].getName().endsWith("HttpStatus")
						|| methods[index].getParameterTypes()[0].getName().endsWith("Status")) {
					Class<?> forName = Class.forName(methods[index].getParameterTypes()[0].getName());
					Object[] enumConstants = forName.getEnumConstants();
					methods[index].invoke(act, enumConstants[0]);
				} else if (methods[index].getParameterTypes()[0].getName().contains("Timestamp")) {
					methods[index].invoke(act,new Timestamp((Date.from(Instant.now())).getTime()));
				} else if (methods[index].getParameterTypes()[0].getName().contains("ZonedDateTime")) {
					methods[index].invoke(act,ZonedDateTime.of(LocalDate.now(), LocalTime.MIN, ZoneId.of("UTC")));
				} else if (methods[index].getParameterTypes()[0].getName().contains("BigDecimal")) {
					methods[index].invoke(act,new BigDecimal(1));
				}
				else {
					Object newInstance = Class.forName(methods[index].getParameterTypes()[0].getName()).newInstance();
					methods[index].invoke(act, newInstance);
				}
			}
		}

		for (int index = 0; index < methods.length; index++) {
			/* Get Data */
			if (!methods[index].getName().equals("getTRNUID")) {
				if (methods[index].getName().startsWith("get")) {
					Object obj1 = methods[index].invoke(act);
					if (obj1 instanceof String) {
						assertEquals("123", obj1);
					}
					if (obj1 instanceof Map) {
						assertNull(((HashMap<?, ?>) obj1).get("123"));
					}
					if (obj1 instanceof Long) {
						assertEquals(123L, obj1);
					}
					if (obj1 instanceof Integer) {
						assertEquals(123, obj1);
					}
				} else if (methods[index].getName().contains("isSuccess")) {
					boolean obj2 = (boolean) methods[index].invoke(act);
					assertTrue(obj2);
				}
			}
			/* To String */
			if (methods[index].getName().contains("toString")) {
				methods[index].invoke(act);
				// return;
			}
		}

		for (int index = 0; index < methods.length; index++) {
			if (methods[index].getName().contains("is")) {
				if (methods[index].getReturnType() == boolean.class && methods[index].getParameterCount() == 0) {
					// methods[index].invoke(act, true);
				}
			}
		}
	}

}
