package cc.oit.generator;

import cc.oit.enums.DBDialectType;
import cc.oit.generator.exception.ConfigException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;

/**
 * Created by Chanedi
 * 单例
 */
public final class GlobalConfig {

    @Getter
    private static GlobalConfig instance = new GlobalConfig();
    private ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();
    /**  */
    @Setter@Getter
    private String outProjectPath = "D:/gen";
    @Setter
    private String inputSqlPath = "classpath:/sql";
    @Setter
    private String tmplPath = "classpath:/tmpl";
    @Setter@Getter
    private String typeMatchConfigPath = "classpath:/typeMatch";
    @Setter@Getter
    private String beanNameRegex = "(\\w+)$";
    @Setter@Getter
    private boolean ignoreExists = true;
    @Setter@Getter
    private DBDialectType dbDialectType = DBDialectType.ORACLE;

    private GlobalConfig() {
        super();
    }

    public File getInputSqlFile() throws ConfigException {
        return getFileByConfig("inputSqlPath", inputSqlPath);
    }

    public File getTmplFile() throws ConfigException {
        return getFileByConfig("tmplPath", tmplPath);
    }

    private File getFileByConfig(String configParamName, String configPath) throws ConfigException {
        Resource resource = resourceLoader.getResource(configPath);
        if (!resource.exists()) {
            throw new ConfigException(configParamName, "路径不存在");
        }
        try {
            return resource.getFile();
        } catch (IOException e) {
            throw new ConfigException(configParamName, e);
        }
    }

}
