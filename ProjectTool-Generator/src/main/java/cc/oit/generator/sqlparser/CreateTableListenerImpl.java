package cc.oit.generator.sqlparser;

import cc.oit.generator.GlobalConfig;
import cc.oit.generator.PropertyTypeContext;
import cc.oit.generator.model.Bean;
import cc.oit.generator.model.Property;
import cc.oit.generator.sqlparser.gen.CreateTableBaseListener;
import cc.oit.generator.sqlparser.gen.CreateTableParser;
import cc.oit.util.StringUtils;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;
import org.mobicents.commons.annotations.NotThreadSafe;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Chanedi
 */
@NotThreadSafe
public class CreateTableListenerImpl extends CreateTableBaseListener {

    @Getter
    private Map<String, Bean> tableMap = new HashMap<String, Bean>(); // key 为 tableName
    private GlobalConfig globalConfig;
    private Bean currentBean;
    private CommentType currentCommentType;

    public CreateTableListenerImpl(GlobalConfig globalConfig) {
        super();
        this.globalConfig = globalConfig;
    }

    public java.util.Collection<Bean> getTables() {
        return tableMap.values();
    }

    @Override
    public void enterMdl(@NotNull CreateTableParser.MdlContext ctx) {
        currentBean = new Bean();
        currentBean.setTableName(ctx.table_name().getText());
        String beanNameRegex = globalConfig.getBeanNameRegex();
        if (beanNameRegex != null) {
            // 根据用户设置修正beanName
            Pattern pattern = Pattern.compile(beanNameRegex);
            Matcher matcher = pattern.matcher(currentBean.getTableName());
            matcher.find();
            String group = matcher.group(matcher.groupCount());
            currentBean.setName(StringUtils.uncapitalizeCamelBySeparator(group, "_"));
        }
        tableMap.put(currentBean.getTableName(), currentBean);
    }

    @Override
    public void enterColumn_definition(@NotNull CreateTableParser.Column_definitionContext ctx) {
        Property column = new Property();
        column.setColumnName(ctx.column_name().getText());
        String datatype = ctx.datatype().getText();
        column.setType(PropertyTypeContext.getInstance().matchPropertyType(datatype));
        currentBean.addProperty(column);
    }

    @Override
    public void enterComment(@NotNull CreateTableParser.CommentContext ctx) {
        String beanTableName = ctx.table_name().getText();
        Bean bean = tableMap.get(beanTableName);
        String comment = ctx.comment_value().getText().replaceAll("'", "");

        CreateTableParser.Column_nameContext column_nameContext = ctx.column_name();
        if (column_nameContext == null) {
            bean.setComment(comment);
        } else {
            String columnName = column_nameContext.getText();
            bean.getPropertyByColumnName(columnName).setComment(comment);
        }

    }

    @Override
    public void enterComment_value(@NotNull CreateTableParser.Comment_valueContext ctx) {
        super.enterComment_value(ctx);
    }

    /*
stringType : 'VARCHAR2' RANGE;
dateType : 'TIMESTAMP' | 'DATE';
doubleType : 'NUMERIC' '(' NUMBER ',' NUMBER ')';
intType : 'NUMERIC' (RANGE | '(' NUMBER ',''0)');
booleanType : 'CHAR(1)';
*/

    private enum CommentType {
        TABLE, COLUMN;
    }

}
