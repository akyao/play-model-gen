package play.modules.modelGen;

import me.stormcat.maven.plugin.s2jdbcgen.DelFlag;

import org.junit.Before;
import org.junit.Test;


public class GeneratorTest {

    @Before
    public void setUp(){
        //TODO targetディレクトリを掃除したい
    }

    @Test
    public void testExecute(){
        Generator executor
            = new Generator("target", "models.abst", "models", "localhost:3306", "ripre", "root", "root", "test\\abstract_model.vm", "test\\model.vm");
        DelFlag delFlag = new DelFlag();
        delFlag.setName("valid");
        delFlag.setDelValue(false);
        executor.setDelFlag(delFlag);
        executor.execute();
    }


}
