/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jashell.tools;

import org.jashell.mock.tools.MockHelper;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileObject.Kind;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CompilerTest {
    private static final String SOURCE_PATH = "./mock-src";
    private static final String OUTPUT_DIR  = "./target";
    private static final String CLASS_PATH  = "./lib/mock-lib.jar";
    private Compiler c;
    public CompilerTest() {
    }

    @Test
    public void testCompilerCreation1(){
        Compiler compiler = Compiler.createCompiler();
        Assert.assertNotNull(compiler);
    }
    
    @Test
    public void testCompilerCreation2(){
        Map<String,String> options = new HashMap<String,String>();
        options.put("-d", "./target");
        options.put("-cp", ".:lib");
        Compiler compiler = Compiler.createCompiler(options);
        Assert.assertNotNull(compiler);
        Assert.assertEquals(compiler.getOptions().size(), options.size());
    }
    
    @Test
    public void testAddOption(){
        c = Compiler.createCompiler()
            .addOption(Compiler.Option.D_DIRECTORY, "./target")
            .addOption(Compiler.Option.SOURCEPATH, SOURCE_PATH);
        Assert.assertNotNull(c.getOptions());
        Assert.assertEquals(c.getOptions().size(), 2);
        Assert.assertTrue(c.getOptions().containsKey("-d"));
        Assert.assertTrue(c.getOptions().containsKey("-sourcepath"));
        c.addOption("-encoding", "utf-8");
        c.addOption(Compiler.Option.SOURCE, "1.6");
        Assert.assertEquals(c.getOptions().size(), 4);
        
        List<String> optionList = c.getOptionsAsList();
        Assert.assertEquals(optionList.size(), 8);
    }
    
    @Test
    public void testAddSourceFile() {
        c = Compiler.createCompiler();
        c.addJavaSource("mock.ClassC000", MockHelper.generateSimpleClassSource("mock", "ClassC000"));
        c.addJavaSource(new File(SOURCE_PATH+"/mock/","C.java"));
        Assert.assertEquals(c.getSourceFiles().size(), 2);
        Assert.assertEquals(c.getSourceFiles().get(0).getKind(), Kind.SOURCE);
    }
    
    @Test
    public void testCompilationWithNoOptions() {
        c = Compiler.createCompiler()
            .addJavaSource(
                new File(SOURCE_PATH+"/mock/","C.java"),
                new File(SOURCE_PATH+"/mock/pack0","A.java"),
                new File(SOURCE_PATH+"/mock/pack1","B.java")
             );
        c.compile();
        Assert.assertTrue(new File(SOURCE_PATH+"/mock/C.class").exists());
        Assert.assertTrue(new File(SOURCE_PATH+"/mock/pack0/A.class").exists());
        Assert.assertTrue(new File(SOURCE_PATH+"/mock/pack1/B.class").exists());
        
        // remove class files
        new File(SOURCE_PATH+"/mock/C.class").delete();
        new File(SOURCE_PATH+"/mock/pack0/A.class").delete();
        new File(SOURCE_PATH+"/mock/pack1/B.class").delete();
    }
    
    @Test
    public void testCompilationWithSourcepathSpecified() throws Exception{
        c = Compiler.createCompiler()
            .addOption(Compiler.Option.SOURCEPATH, SOURCE_PATH)
            .addOption(Compiler.Option.D_DIRECTORY, OUTPUT_DIR);
        c.compile();
        Assert.assertTrue(new File(OUTPUT_DIR+"/mock/C.class").exists());
        Assert.assertTrue(new File(OUTPUT_DIR+"/mock/pack0/A.class").exists());
        Assert.assertTrue(new File(OUTPUT_DIR+"/mock/pack1/B.class").exists());
    }
    
    @Test
    public void testCompilationWithClasspathSpecified() {
        c = Compiler.createCompiler()
            .addOption(Compiler.Option.CLASSPATH, CLASS_PATH)
            .addOption(Compiler.Option.D_DIRECTORY, OUTPUT_DIR);
        c.addJavaSource("mock.ClassF000", MockHelper.generateClassSourceWithImport("mock", "ClassF000"));
        c.compile();
        for (CompilationDiagnostic diag : c.getCompilationDiagnostics()){
            System.out.println (diag);
        }
        Assert.assertTrue(c.getCompilationDiagnostics().size() == 0);
    }
    
    @Test
    public void testCompilationDiagnostics() {
        c = Compiler.createCompiler();
        
        // compile broken code
        c.addJavaSource("mock.ClassD000", MockHelper.generateBrokenClassSource("mock", "ClassD000"));
        c.compile();
        List<CompilationDiagnostic> diags = c.getCompilationDiagnostics();
        Assert.assertTrue(diags.size() > 0);
        
        // compile OK code
        c = Compiler.createCompiler();
        c.addJavaSource("mock.ClassE000", MockHelper.generateSimpleClassSource("mock", "ClassE000"));
        c.compile();
        diags = c.getCompilationDiagnostics();
        Assert.assertTrue(diags.size() == 0);
        for(CompilationDiagnostic diag : diags){
            System.out.println (diag.toString());
        }
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
