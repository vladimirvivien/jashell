package org.jashell.tools;

public class MockHelper {
    public static String generateSimpleClassSource(String packageName, String className){
        return "package " + packageName +";"
            + "public class " + className + "{"
            + "    public static final void f(){}"
            + "}";
    }
}
