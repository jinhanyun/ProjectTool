package cc.oit.generator;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chanedi
 */
public class TemplateRoot {

    @Getter
    private TemplateRootConfig config;
    @Getter
    private List<String> templateNames;

    public TemplateRoot(File rootDir) throws IOException {
        config = TemplateRootConfig.getInstance(rootDir.getAbsolutePath());
        templateNames = new ArrayList<String>();

        parseTemplateNames(rootDir, rootDir.getName());
    }

    private void parseTemplateNames(File dir, String namePath) {
        File[] files = dir.listFiles();
        for (File file : files) {
            String name = namePath + "/" + file.getName();
            if (file.isDirectory()) {
                parseTemplateNames(file, name);
            } else if (!file.getName().equals(TemplateRootConfig.CONFIG_FILE_NAME)) {
                templateNames.add(name);
            }
        }
    }

}
