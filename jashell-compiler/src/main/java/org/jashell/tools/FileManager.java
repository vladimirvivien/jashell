package org.jashell.tools;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

/**
 * A JavaFileManager implementation used to manage JavaFileObject during
 * compilation.  It extends the ForwardingJavaFileManager and has the ability
 * to manage JavaFileObject backed by the Filesystem or StringSourceFile for 
 * JavaFileObject managed in memory.
 * @author vladimir vivien
 */
public class FileManager extends ForwardingJavaFileManager<JavaFileManager>{
    private Map<URI, JavaFileObject> srcStore = new HashMap<URI,JavaFileObject>();
    private Map<URI, JavaFileObject> classStore = new HashMap<URI,JavaFileObject>();
    private ClassLoader classLoader;
    private boolean inMemory = false;
    
    public static URI toUri(String path){
        if(path == null) {
            return null;
        }
        return URI.create("mem://" + path);
    }
    
    public static URI toUri(final String classFQN, final Kind fileKind){
        String path = extractPackageFromFQN(classFQN).replaceAll("\\.", "/");
        String className = extractClassFromFQN(classFQN) + fileKind.extension;
        return toUri(path + "/" + className);
    }
    
    public static String extractClassFromFQN(final String classFQN){
        int pos = (classFQN != null) ? classFQN.lastIndexOf(".") : -1;
        return (pos == -1) ? classFQN : classFQN.substring(pos+1);
    }
    
    public static String extractPackageFromFQN(final String classFQN){
        int pos = (classFQN != null) ? classFQN.lastIndexOf(".") : -1;
        return (pos == -1) ? "" : classFQN.substring(0, pos); 
    }
    
    public static FileManager createInstance(final JavaFileManager jfm){
        FileManager fm = new FileManager(jfm);
        return fm;
    }
    
    public static FileManager createInMemoryInstance(final JavaFileManager jfm){
        FileManager fm = new FileManager(jfm, true);
        return fm;
    }
    
    
    private FileManager(JavaFileManager parentFileManager){
        super(parentFileManager);
        inMemory = false;
        classLoader = new InMemClassLoader(this);
    }

    private FileManager(JavaFileManager parentFileManager, boolean inMemFlag){
        super(parentFileManager);
        this.inMemory = inMemFlag;
        if(inMemory){
            classLoader = new InMemClassLoader(this);
        }
    }
       
    public void addSourceFile(final JavaFileObject file){
        if(file != null){
            srcStore.put(file.toUri(), file);
        }
    }
    
    public void addSourceFiles(List<JavaFileObject> files){
        for(JavaFileObject jfo : files){
            addSourceFile(jfo);
        }
    }
    
    public JavaFileObject getSourceFile(URI uri){
        return srcStore.get(uri);
    }
    
    public List<JavaFileObject> getAllSourceFiles() {
        List<JavaFileObject> result = new ArrayList<JavaFileObject>(srcStore.size());
        for(Map.Entry<URI, JavaFileObject> e : srcStore.entrySet()){
            JavaFileObject jfo = e.getValue();
            if(jfo != null){
                result.add(jfo);
            }
        }
        return result;
    }    
    
    public JavaFileObject getClassFile(String classFQN){
        assertInMemFlag();
        return classStore.get(toUri(classFQN, Kind.CLASS));
    }
    
    @Override 
    public FileObject getFileForInput(JavaFileManager.Location location, 
            String packageName, String relativeName) throws IOException{
        if(inMemory){
            return srcStore.get(toUri(packageName+relativeName));
        }
        return super.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
            String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException{
        if(inMemory){
            StringSourceFile classFile =  StringSourceFile.createInstanceForClass(className);
            classStore.put(toUri(className,kind), classFile);
            return classFile;
        }
        return super.getJavaFileForOutput(location, className, kind, sibling);
    }
    
    @Override
    public String inferBinaryName(JavaFileManager.Location location, JavaFileObject file){
        return (file instanceof StringSourceFile) ? file.getName() : super.inferBinaryName(location, file);
    }
   
    @Override
    public Iterable<JavaFileObject> list(JavaFileManager.Location location,
            String packageName, Set<JavaFileObject.Kind> kinds,
            boolean recurse) throws IOException{
        
        if(inMemory){
            ArrayList<JavaFileObject> result = new ArrayList<JavaFileObject>();
            if(kinds.contains(Kind.CLASS)){
                for(JavaFileObject o : classStore.values()){
                    if(o.getName().contains(packageName)){
                        result.add(o);
                    }
                }
            }

            if(kinds.contains(Kind.SOURCE)){
                for(JavaFileObject o : srcStore.values()){
                    if(o.getName().contains(packageName)){
                        result.add(o);
                    }
                }
            }
        

            for(JavaFileObject jfo : super.list(location, packageName, kinds,recurse)){
                result.add(jfo);
            }

            return result;
        }
        
        return super.list(location, packageName, kinds,recurse);
    }
    
    
    @Override
    public ClassLoader getClassLoader(JavaFileManager.Location location){
        if(inMemory){
            return classLoader;
        }
        return super.getClassLoader(location);
    }
    
    private void assertInMemFlag(){
        if(!inMemory) {
            throw new java.lang.UnsupportedOperationException(
                "Unable to complete operation: FileManager not set for in-memory management.");
        }        
    }
    
    private class InMemClassLoader extends ClassLoader{
        private FileManager fileManager;
        public InMemClassLoader(FileManager fm){
            super (fm.getClass().getClassLoader());
            fileManager = fm;
        }
        
        @Override
        protected Class<?> findClass (String classFQN) throws ClassNotFoundException {
            JavaFileObject jfo = fileManager.getClassFile(classFQN);
            
            if(jfo == null){
                throw new ClassNotFoundException (classFQN);
            }
            
            byte[] byteCode = ((StringSourceFile)jfo).getByteCode();
            Class cl = defineClass(classFQN, byteCode, 0, byteCode.length);

            if(cl == null){
                throw new ClassNotFoundException (classFQN);
            }

            return cl;
        }
    }
    
}
