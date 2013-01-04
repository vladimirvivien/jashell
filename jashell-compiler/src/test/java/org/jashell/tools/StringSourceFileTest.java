package org.jashell.tools;

import java.io.InputStream;
import java.util.Arrays;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.apache.bcel.classfile.JavaClass;
import org.testng.annotations.Test;
import org.testng.Assert;

public class StringSourceFileTest {
    final String PACKAGE_NAME = "mock";
    final String CLASS_NAME = "InMemoryTestClass";
    final String CLASS_FQN = PACKAGE_NAME +"." + CLASS_NAME;
    final String SOURCE_CODE =
              "package " + PACKAGE_NAME +";"
            + "public class " + CLASS_NAME + "{"
            + "    public static final void f(){}"
            + "}";
    
    public StringSourceFileTest() {}
    
    @Test
    public void testCreateForSourceCode() throws Exception{
        StringSourceFile f = StringSourceFile.createInstanceForSource(CLASS_FQN, SOURCE_CODE);
        Assert.assertEquals(f.getClassFQN(), CLASS_FQN);
        Assert.assertEquals(f.getCharContent(true), SOURCE_CODE);
        Assert.assertEquals(f.getKind(), Kind.SOURCE);
        Assert.assertEquals(f.toUri(), FileManager.toUri(CLASS_FQN,Kind.SOURCE));
    }

    @Test
    public void testCreateForClass() throws Exception{
        StringSourceFile f = StringSourceFile.createInstanceForClass(CLASS_FQN);
        Assert.assertEquals(f.getClassFQN(), CLASS_FQN);
        Assert.assertEquals(f.getKind(), Kind.CLASS);
        Assert.assertEquals(f.toUri(), FileManager.toUri(CLASS_FQN,Kind.CLASS));
    }
    
    @Test
    public void testCompilationIntegration() throws Exception{
        StringSourceFile f = StringSourceFile.createInstanceForSource(CLASS_FQN, SOURCE_CODE);
        MockCompiler javac = new MockCompiler();
        Boolean compiled = javac.compile(f);
        Assert.assertTrue(compiled);
        
        // get bytecode from mock compiled object
        JavaFileObject byteCode = javac.getJavaOutputFile(CLASS_FQN);
        Assert.assertNotNull(byteCode);
        InputStream byteCodeStream = byteCode.openInputStream();
        
        Assert.assertNotNull(byteCodeStream != null);
        JavaClass jc = MockCompiler.parseByteCode(CLASS_FQN , byteCodeStream);
        Assert.assertEquals(jc.getClassName(), CLASS_FQN);
        Assert.assertEquals(jc.getMethods().length, 2);
        Assert.assertTrue(jc.getMethods()[1].getName().equals("f"));
    }
    
}
