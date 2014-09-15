import cc.oit.generator.FilesGenerator;
import cc.oit.generator.GlobalConfig;
import cc.oit.generator.exception.ConfigException;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by Chanedi
 */
public class GeneratorTest extends TestCase {

    @Test
    public void testGenerator() throws ConfigException {
        FilesGenerator generator = new FilesGenerator();
        GlobalConfig globalConfig = generator.getGlobalConfig();
        globalConfig.setOutProjectPath("E:/Chanedi/IdeaProjects/ProjectTool/ProjectTool-Generator");
        globalConfig.setBeanNameRegex("^T_[A-Z]{3}_(\\w+)$");
        globalConfig.setIgnoreExists(false);

        generator.process();
    }

}
