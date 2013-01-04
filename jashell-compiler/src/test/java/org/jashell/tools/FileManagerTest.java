/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jashell.tools;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.bcel.classfile.JavaClass;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author vvivien
 */
public class FileManagerTest {
    private JavaCompiler toolCompiler = ToolProvider.getSystemJavaCompiler();
    private StandardJavaFileManager jfm = toolCompiler.getStandardFileManager(null, null, null);
    final String SOURCE_PATH = "./mock-src";
    final String PACKAGE_NAME = "mock.filemanager";
    
    public FileManagerTest() {
    }
    
    @Test
    public void testExtractClassName(){
        String classFQN = "test.package.TestClass";
        Assert.assertEquals("TestClass", FileManager.extractClassFromFQN(classFQN));
        classFQN = "TestClass";
        Assert.assertEquals("TestClass", FileManager.extractClassFromFQN(classFQN));
    }
    
    @Test
    public void testExtractPackageName() {
        String classFQN = "test.package.TestClass";
        Assert.assertEquals("test.package", FileManager.extractPackageFromFQN(classFQN));
        classFQN = "TestClass";
        Assert.assertTrue(FileManager.extractPackageFromFQN(classFQN).length() == 0);
    }
    
    @Test
    public void testToUriFromPath() {
        String path = "test/package/TestClass.java";
        Assert.assertEquals(FileManager.toUri(path), URI.create("mem://" + path));
    }
    
    @Test
    public void testToUriFromFQNplusKind(){
        String classFQN = "test.package.TestClass";
        Kind kind = Kind.SOURCE;
        URI uri = FileManager.toUri(classFQN, kind);
        Assert.assertEquals (uri, URI.create("mem://test/package/TestClass.java"));
    }
    
    @Test
    public void testSourceFileStorage() {
        FileManager fm = FileManager.createInstance(jfm);
        File[] sources = { 
            new File(SOURCE_PATH+"/mock/","C.java"),
            new File(SOURCE_PATH+"/mock/pack0/", "A.java"),
            new File(SOURCE_PATH+"/mock/pack1/", "B.java")
        };
        
        Iterable<? extends JavaFileObject> jfos = jfm.getJavaFileObjects(sources);
        fm.addSourceFiles((List<JavaFileObject>)jfos);
        Assert.assertEquals(fm.getAllSourceFiles().size(), 3);
        Assert.assertTrue(fm.getAllSourceFiles().get(0).getName().contains("C.java"));
    }
    
    @Test
    public void testInMemoryCompilation() throws Exception{
        String className = "ClassA000";
        String classFqn  = PACKAGE_NAME + "." + className;
        
        Assert.assertNotNull(toolCompiler);
        FileManager fm = FileManager.createInMemoryInstance(jfm);
        
        String sourceCode = MockHelper.generateSimpleClassSource(PACKAGE_NAME, className);
        
        StringSourceFile f = StringSourceFile.createInstanceForSource(classFqn, sourceCode);
        fm.addSourceFile(f);
        MockCompiler javac = new MockCompiler(fm);
        Assert.assertTrue(javac.getJavaFileManager().getClass().isAssignableFrom(FileManager.class));
        Boolean compiled = javac.compile(f);
        Assert.assertTrue(compiled);
        
        // get bytecode from mock compiled object
        JavaFileObject byteCode = fm.getClassFile(classFqn);
        Assert.assertNotNull(byteCode);
        InputStream byteCodeStream = byteCode.openInputStream();
        
        Assert.assertNotNull(byteCodeStream != null);
        JavaClass jc = MockCompiler.parseByteCode(classFqn , byteCodeStream);
        Assert.assertEquals(jc.getClassName(), classFqn);
        Assert.assertEquals(jc.getMethods().length, 2);
        Assert.assertTrue(jc.getMethods()[1].getName().equals("f"));
        
        // ensure file not written to file system
        File classFile = new File("./target/" + PACKAGE_NAME.replaceAll("\\.", "/"), className + ".class");
        Assert.assertTrue(!classFile.exists());
        
    }

    @Test
    public void testCompilationFromFileSystem() throws Exception{
        String className = "ClassB000";
        String classFqn  = PACKAGE_NAME + "." + className;
        String sourceCode = MockHelper.generateSimpleClassSource(PACKAGE_NAME, className);
        
        Assert.assertNotNull(toolCompiler);
        FileManager fm = FileManager.createInstance(jfm);
        
        StringSourceFile f = StringSourceFile.createInstanceForSource(classFqn, sourceCode);
        MockCompiler javac = new MockCompiler(fm);
        Assert.assertTrue(javac.getJavaFileManager().getClass().isAssignableFrom(FileManager.class));
        Boolean compiled = javac.compile(f);
        Assert.assertTrue(compiled);
        
        // get bytecode from mock compiled object
        JavaFileObject byteCode = javac.getJavaOutputFile(classFqn);
        Assert.assertNotNull(byteCode);
        InputStream byteCodeStream = byteCode.openInputStream();
        
        Assert.assertNotNull(byteCodeStream != null);
        JavaClass jc = MockCompiler.parseByteCode(classFqn , byteCodeStream);
        Assert.assertEquals(jc.getClassName(), classFqn);
        Assert.assertEquals(jc.getMethods().length, 2);
        Assert.assertTrue(jc.getMethods()[1].getName().equals("f"));
        
        // ensure class file is written to file system
        File classFile = new File("./target/" + PACKAGE_NAME.replaceAll("\\.", "/"), className + ".class");
        Assert.assertTrue(classFile.exists());
    }
    
    @Test
    public void testInMemFileManagerClassLoader() throws Exception {
        String className = "ClassC000";
        String classFqn  = PACKAGE_NAME + "." + className;
        
        Assert.assertNotNull(toolCompiler);
        FileManager fm = FileManager.createInMemoryInstance(jfm);
        
        String sourceCode = MockHelper.generateSimpleClassSource(PACKAGE_NAME, className);
        
        StringSourceFile f = StringSourceFile.createInstanceForSource(classFqn, sourceCode);
        fm.addSourceFile(f);
        MockCompiler javac = new MockCompiler(fm);
        Assert.assertTrue(javac.getJavaFileManager().getClass().isAssignableFrom(FileManager.class));
        Boolean compiled = javac.compile(f);
        Assert.assertTrue(compiled);
        
        // make sure no class file generate in file system
        File classFile = new File("./target/" + PACKAGE_NAME.replaceAll("\\.", "/"), className + ".class");
        Assert.assertTrue(!classFile.exists());
        
        ClassLoader cl = fm.getClassLoader(null);
        Assert.assertNotNull (cl);
        Class mockClass = cl.loadClass(classFqn);
        Assert.assertNotNull(mockClass);
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
}
