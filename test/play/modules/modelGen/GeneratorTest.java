package play.modules.modelGen;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.stormcat.maven.plugin.s2jdbcgen.DelFlag;

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
        //TODO Abstractを継承している箇所でコンパイルエラー
        //TODO classファイルはいらない。コードに問題ないことだけ確認できればいい
        if(file.exists()){
            if(file.isDirectory()){
                classPaths.add(file);
                for(File sub : file.listFiles()){
                    verifyGeneratedCode(sub);
                }
            }
            if("java".equals(getExtension(file.getName()))){
                Main main = new Main();
                String[] string = {"-verbose", file.getPath(), "-classpath", getCp()};
                int result = main.compile(string);
                assertEquals(0, result);
            }
        }
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

    private String getCp(){

        if(classPaths.isEmpty()){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("");
        for(File dir : classPaths){
            builder.append(dir.getPath() + ";");
        }
        return builder.toString();
    }
}