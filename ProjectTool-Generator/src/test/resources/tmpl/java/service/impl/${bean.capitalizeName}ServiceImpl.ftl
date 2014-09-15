package cc.oit.${module.name}.service.impl;

import cc.oit.service.EntityServiceImpl;
import cc.oit.${module.name}.service.${bean.capitalizeName}Service;
import cc.oit.${module.name}.model.${bean.capitalizeName};
import org.springframework.stereotype.Service;

@Service
public class ${bean.capitalizeName}ServiceImpl extends EntityServiceImpl<${bean.capitalizeName}> implements ${bean.capitalizeName}Service {
}
