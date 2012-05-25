package play.modules.modelGen;

import static org.junit.Assert.*;

import java.io.File;

import me.stormcat.maven.plugin.s2jdbcgen.DelFlag;

import org.junit.Before;
import org.junit.Test;


public class GeneratorTest {

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
        Generator executor
            = new Generator("target", "models.abst", "models", "localhost:3306", "modelgen_test", "root", "root", "test\\abstract_model.vm", "test\\model.vm");
        DelFlag delFlag = new DelFlag();
        delFlag.setName("valid");
        delFlag.setDelValue(false);
        executor.setDelFlag(delFlag);
        executor.execute();
    }


}
