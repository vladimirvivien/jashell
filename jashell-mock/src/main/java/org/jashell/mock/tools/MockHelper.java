package org.jashell.mock.tools;

public class MockHelper {
    public static String generateSimpleClassSource(String packageName, String className){
        return "package " + packageName +";"
            + "public class " + className + "{"
            + "    public static final void f(){}"
            + "}";
    }
    
    public static String generateBrokenClassSource(String packageName, String className){
        return "package " + packageName +";"
            + "public class " + className + "{"
            + "    public static final void f(){ int a = b}"
            + "}";
        
    }
    
    public static String generateClassSourceWithImport(String packageName, String className){
        return "package " + packageName +";"
            + "import org.jashell.mock.source.Do;"
            + "public class " + className + "{"
            + "    public static final void doTheDo(){"
                + "    Do d = new Do();"
                + "    d.something();"
                + "}"
            + "}";
        
    }    
}
