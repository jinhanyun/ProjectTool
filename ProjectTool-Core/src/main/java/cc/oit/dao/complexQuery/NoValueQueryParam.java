package cc.oit.dao.complexQuery;

import lombok.Getter;

/**
 * Created by Chanedi
 */
public class NoValueQueryParam extends CustomQueryParam {

    @Getter
    private String condition;

    public NoValueQueryParam(String property, String condition) {
        super.property = property;
        this.condition = condition;
    }
}
