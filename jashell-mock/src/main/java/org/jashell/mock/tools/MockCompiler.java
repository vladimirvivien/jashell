package org.jashell.mock.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

public class MockCompiler {
    private static final String OUTPUT_DIR = "./target";
    private static final String[] COMPILER_OPTIONS={
        "-d", OUTPUT_DIR
    };
    
    private JavaCompiler javac ;
    private JavaFileManager jfm;
    private Writer javacOut = new PrintWriter(System.out);

    public MockCompiler() {
        javac = ToolProvider.getSystemJavaCompiler();
        jfm = javac.getStandardFileManager(null, null, null);
    }
    
    public MockCompiler (JavaFileManager fileManager){
        this();
        jfm = fileManager;
    }
    
    public JavaCompiler getJavaCompiler() {
        return javac;
    }
    
    public JavaFileManager getJavaFileManager() {
        return jfm;
    }
            
    public Boolean compile (List<JavaFileObject> sourceFiles){
         return javac.getTask(javacOut, jfm, null, null, null, sourceFiles).call();
    }
    public Boolean compile (JavaFileObject jfo){
         return javac.getTask(javacOut, jfm, null, Arrays.asList(COMPILER_OPTIONS), null, Arrays.asList(jfo)).call();
    }
    
    public JavaFileObject getJavaOutputFile(String className){
        try{
            return jfm.getJavaFileForOutput(javax.tools.StandardLocation.CLASS_OUTPUT, className, JavaFileObject.Kind.CLASS, null);
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
    
    public static JavaClass parseByteCode(String className, InputStream in){
        ClassParser parser = new ClassParser(in, className);
        try{
            return parser.parse();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}