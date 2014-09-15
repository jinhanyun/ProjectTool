package cc.oit.${module.name}.model;

import java.util.*;
import lombok.Data;
import cc.oit.model.Entity;

@Data
public class ${bean.capitalizeName} extends Entity {

    ${generate.serialVersionUID}

    <#list bean.properties as prop>
    private ${prop.type.java} ${prop.name};
    </#list>

}
