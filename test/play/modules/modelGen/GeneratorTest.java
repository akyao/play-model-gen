package play.modules.modelGen;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.stormcat.maven.plugin.s2jdbcgen.DelFlag;

import org.apache.commons.collections.ListUtils;
import org.junit.Before;
import org.junit.Test;
import com.sun.tools.javac.Main;

public class GeneratorTest {

    private Set<File> classPaths = new HashSet<File>();

    @Before
    public void setUp(){
        File modelsDir = new File("target\\models");
        deleteRecursively(modelsDir);
    }

    public void deleteRecursively(File file){
        if(file.exists()){
            if(file.isDirectory()){
                for(File sub : file.listFiles()){
                    deleteRecursively(sub);
                }
            }
            assertTrue(file.delete());
        }
    }

    @Test
    public void testExecute(){
        //自動生成
        Generator executor
            = new Generator("target", "models.abst", "models", "localhost:3306", "modelgen_test", "root", "root", "test\\abstract_model.vm", "test\\model.vm");
        DelFlag delFlag = new DelFlag();
        delFlag.setName("valid");
        delFlag.setDelValue(false);
        executor.setDelFlag(delFlag);
        executor.execute();

        verifyGeneratedCode(new File("target//models"));
    }

    private void verifyGeneratedCode(File file){
        List<String> files = getJavaFiles(file);
        //javaファイル数は6個できていること
        assertEquals(6, files.size());
        //コンパイルでエラーとならないこと
        Main main = new Main();
        int result = main.compile((String[])files.toArray(new String[0]));
        assertEquals(0, result);
    }

    private List<String> getJavaFiles(File file){
        List<String> files = new ArrayList<String>();
        if(file.exists()){
            if(file.isDirectory()){
                for(File sub : file.listFiles()){
                    files.addAll(getJavaFiles(sub));
                }
            }
            if("java".equals(getExtension(file.getName()))){
                files.add(file.getPath());            }
        }
        return files;
    }

    private String getExtension(String fileName){
        if (fileName == null)
            return null;
        int point = fileName.lastIndexOf(".");
        if (point != -1) {
            return fileName.substring(point + 1);
        }
        return "";
    }
}