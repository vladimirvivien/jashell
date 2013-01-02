package org.jashell.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

/**
 * This class can represent either a Java source file or a compiled byte code.
 * It uses URI with scheme mem: (mem://classFQN+KIND) to represent JavaFileObject URI.
 * @author vladimir vivien
 */
public class InMemoryFile extends SimpleJavaFileObject {
    private ByteArrayOutputStream byteCodeStore = new ByteArrayOutputStream();;
    private String classFQN;
    private URI classUri ;
    private String sourceCode ;
 
    /**
     * Creates a InMemoryJavaFileObject of Kind.SOURCE.
     * @param uri - the URI for the source file.
     * @param code - the source code.
     */
    private InMemoryFile(final URI uri, final String fqn, final String code) {
        super(uri, JavaFileObject.Kind.SOURCE);
        classUri = uri ;
        this.classFQN = fqn;
        this.sourceCode = code ;
    }
 
    /**
     * Creates an instance of InMemoryJavaFileObject of KIND.CLASS.
     * @param uri - the URI for the class file. 
     */
    private InMemoryFile (final URI uri, final String fqn){
        super(uri, Kind.CLASS);
        classUri = uri;
        classFQN = fqn;
    }
    
    /**
     * Static factory method to create an InMemoryFile of type KIND.SOURCE.
     * @param classFQN - class fully-qualified class name (package.ClassName)
     * @param source - the java source code represented by that file
     * @return 
     */
    public static InMemoryFile createInstanceForSource(final String classFQN, final String source){
        URI uri = FileManager.toUri(classFQN,Kind.SOURCE);
        if(source == null){
            throw new RuntimeException ("Source cannot be null.");
        }
        
        return new InMemoryFile(uri, classFQN, source);
    }
    
    /**
     * Static factory method to create an InMemoryFile of KIND.CLASS.
     * @param classFQN - fully-qualified class name
     * @return 
     */
    public static InMemoryFile createInstanceForClass(final String classFQN){
        URI uri = FileManager.toUri(classFQN,Kind.CLASS);
        return new InMemoryFile(uri, classFQN);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
            throws IOException {
        return sourceCode;
    }
    
    /**
     * Returns a stream for accessing the byte code for file object.
     * @return InputStream for reading file object bytecode.
     * @throws IOException 
     */
    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(byteCodeStore.toByteArray());
    }
    
    /**
     * A opens a stream for writing bytecode for file object
     * @return an OutputStream to write bytecode to.
     * @throws IOException 
     */
    @Override
    public OutputStream openOutputStream() throws IOException {
        byteCodeStore = new ByteArrayOutputStream();
        return byteCodeStore;
    }
    
    /**
     * Returns the bytecode content for java file object.
     * @return byte[]
     */
    public byte[] getByteCode() {
        return (byteCodeStore != null) ? 
                byteCodeStore.toByteArray() : 
                null;
    }
    
    /**
     * Returns the JavaFileObject fully-qualified name of the class.
     * @return 
     */
    public String getClassFQN() {
        return classFQN;
    }
    
}