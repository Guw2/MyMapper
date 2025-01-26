package mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Mapper {
	
	private static Class<?> classe;
	
	private static String serializedObject = "";
	
	private static String surroundWithQuotes(Object obj) {
		return "\"" + obj + "\"";
	}
	
	private static Integer parameterQuantity(String json) {
		return json.split("\n").length - 2;
	}
	
	private static String[] getJsonValues(String json) {
		String[] values = new String[parameterQuantity(json)];
		
		for(int c = 0; c < values.length; c++) {
			values[c] = json.split("\n")[c+1].split(":")[1].replaceAll(",", "").replaceAll("\"", "").strip();
		}
		
		return values;
	}
	
	private static String[] getJsonFields(String json) {
		
		String[] fields = new String[parameterQuantity(json)];
		
		for(int c = 0; c < fields.length; c++) {
			fields[c] = json.split("\n")[c+1].split(":")[0];
		}
		
		return fields;
	}
	
	public static String serialize(Object obj) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

		classe = obj.getClass();
		serializedObject = serializedObject.concat("{\n");
		
		for(Field s : classe.getDeclaredFields()) {
			String field = s.getName().substring(0, 1).toUpperCase() + s.getName().substring(1);
			Method method = classe.getDeclaredMethod(String.format("get%s", field));
			method.setAccessible(true);
			serializedObject = serializedObject.concat("    \"" + s.getName() + "\" : ");
			if(s.getType().getName() == "java.lang.String")
				serializedObject = serializedObject.concat(surroundWithQuotes(method.invoke(obj)) + "\n");
			else
				serializedObject = serializedObject.concat(method.invoke(obj)+"\n");
		}
		serializedObject = serializedObject.concat("}");
		
		return serializedObject;
	}
	
	public static Object desserialize(String json, Class<?> classe) throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		
		String[] fields = getJsonFields(json);
		String[] values = getJsonValues(json);
		Constructor<?> constructor = classe.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object obj = constructor.newInstance();
		
		for(int c = 0; c < parameterQuantity(json); c++) {
			String fieldUpperCase = fields[c].strip().replaceAll("\"", "");
			fieldUpperCase = fieldUpperCase.substring(0, 1).toUpperCase() + fieldUpperCase.substring(1);
			fieldUpperCase = "set"+fieldUpperCase;
			Class<?> tipo = null;
			
			for(Method m : classe.getDeclaredMethods()) {
				if(m.toString().contains(fieldUpperCase)) {					
					tipo = m.getParameterTypes()[0];
				}
			}
			
			Method method = obj.getClass().getDeclaredMethod(fieldUpperCase, tipo);
			method.setAccessible(true);
			if(tipo == Integer.class)
				method.invoke(obj, Integer.parseInt(values[c].strip()));				
			else if(tipo == Double.class)
				method.invoke(obj, Double.parseDouble(values[c].strip()));
			else if(tipo == Float.class)
				method.invoke(obj, Float.parseFloat(values[c].strip()));
			else if(tipo == Boolean.class)
				method.invoke(obj, Boolean.parseBoolean(values[c].strip()));
			else
				method.invoke(obj, values[c]);
		}
		
		return obj;
	}
}
