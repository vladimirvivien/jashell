/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jashell.tools;

import com.sun.media.jai.opimage.AddCRIF;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author vvivien
 */
public class Compiler {
    public static enum Option {
        G("g"),
        G_NONE ("g:none"),
        G_LINES("g:lines"),
        G_VARS("g:vars"),
        G_SOURCE("g:source"),
        NO_WARN("nowarn"),
        VERBOSE("verbose"),
        DEPRECATION("deprecation"),
        CLASSPATH("classpath"),
        SOURCEPATH("sourcepath"),
        BOOT_CLASSPATH("bootclasspath"),
        EXT_DIRS("extdirs"),
        ENDORSED_DIRS("endorseddirs"),
        PROC_NONE("proc:none"),
        PROC_ONLY("proc:only"),
        PROCESSOR("processor"),
        PROCESSOR_PATH("processorpath"),
        D_DIRECTORY("d"),
        S_DIRECTORY("s"),
        IMPLICIT_NONE("implicit:none"),
        IMPLICIT_CLASS("implicit:class"),
        ENCODING("encoding"),
        SOURCE("source"),
        TARGET("target"),
        VERSION("vesion"),
        X_OPTION("X"),
        W_ERROR("Werror")
        ;
        private String option;
        Option(String name){
            option = name;
        }
        String get() {
            return "-"+option;
        }

    }
    
    private JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private StandardJavaFileManager jfm = javac.getStandardFileManager(null, null, null);
    private Map<String,String> options = new LinkedHashMap<String,String>();
    private FileManager fileManager;
    //private List<JavaFileObject> sourcepath = new ArrayList<JavaFileObject>();
    private Writer javacOut = new PrintWriter(System.out);
    
    private Compiler() {
        assertCompilerTool();
        fileManager = FileManager.createInstance(jfm);
    }
    
    private Compiler(Map<String,String> ops){
        this();
        options = ops;
    }
    
    static public Compiler createCompiler(){
        Compiler c = new Compiler();
        return c;
    }
    
    static public Compiler createCompiler(final Map<String,String> options){
        Compiler c = new Compiler(options);
        return c;
    }
    
    /**
     * Adds an javac compiler option.
     * @param op option type
     * @param value value
     * @return 
     */
    public Compiler addOption (final Option op, final String value){
        addOption(op.get(), value);
        return this;
    }
    
    /**
     * Collects standard javac options.  If option is -d for sourcepath, 
     * this method also collect all javafiles.
     * @param option
     * @param value
     * @return 
     */
    public Compiler addOption(final String option, final String value){
        options.put(option, value);
        if(option.equals(Option.SOURCEPATH.get())){
            collectJavaSourceFilesFromDir(new File(value));
        }
        return this;
    }
    public Map<String,String> getOptions(){
        return Collections.unmodifiableMap(options);
    }
    
    public List<String> getOptionsAsList() {
        List<String> result = new ArrayList<String>(options.size());
        for(Map.Entry<String,String> e : options.entrySet()){
            result.add(e.getKey());
            String val = e.getValue();
            if(val != null){
                result.add(e.getValue());
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Stores the source string as a Java source file to the source path.
     * @param classFQN the fully qualified class name
     * @param source the String value representing the java source.
     * @return 
     */
    public Compiler addJavaSource(final String classFQN, final String source){
        if(classFQN == null || source == null) return this;
        StringSourceFile f = StringSourceFile.createInstanceForSource(classFQN, source);
        fileManager.addSourceFile(f);
        return this;
    }
    
    /**
     * Designed to add multiple String values as Java sources to the source path.
     * @param sources a map where key=classFQN and value=java source code
     * @return 
     * @see org.jashell.tools.Compiler#addJavaSources(java.lang.String, java.lang.String) 
     */
    public Compiler addJavaSources(final Map<String,String> sources){
        if(sources == null || sources.size() == 0) return this;
        for(Map.Entry<String,String> e : sources.entrySet()){
            addJavaSource(e.getKey(), e.getValue());
        }
        
        return this;
    }
    
    /**
     * Adds an array of File objects as Java source files to the source path.
     * @param files array of File instances
     * @return 
     */
    public Compiler addJavaSource(final File ... files){
        Iterable<? extends JavaFileObject> jfos = jfm.getJavaFileObjects(files);
        fileManager.addSourceFiles((List<JavaFileObject>)jfos);
        return this;
    }
    
    public List<JavaFileObject> getSourceFiles() {
        return Collections.unmodifiableList(fileManager.getAllSourceFiles());
    }
        
    /**
     * Compiles the provided sourcepath or the path specified in options.
     */
    public void compile(){
        CompilationTask task = javac.getTask(
                javacOut, 
                fileManager, 
                null, 
                getOptionsAsList(), 
                null, 
                fileManager.getAllSourceFiles());
        Boolean compiled = task.call();
    }

    /**
     * Walk directory depth to collect files starting from rootDir.
     * @param rootDir
     * @return 
     */
    private void collectJavaSourceFilesFromDir(File rootDir){
        File[] dirList = rootDir.listFiles();
        List<File> collectedFiles = new ArrayList();
        for(File f : dirList){
            if(f.isDirectory()){
                collectJavaSourceFilesFromDir(f);
            }
            if(f.isFile() && f.getName().endsWith(Kind.SOURCE.extension)){
                collectedFiles.add(f);
            }
        }
        List<JavaFileObject> jfos = (List<JavaFileObject>) jfm.getJavaFileObjectsFromFiles(collectedFiles);
        fileManager.addSourceFiles(jfos);
    }
    
    private void assertCompilerTool() {
        if(javac == null){
            throw new IllegalStateException("Compiler support not found.");
        }
    }
    
   
}