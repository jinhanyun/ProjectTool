package cc.oit.dao.impl.mybatis;

import cc.oit.dao.complexQuery.CustomQueryParam;
import cc.oit.dao.complexQuery.NoValueQueryParam;
import cc.oit.dao.complexQuery.Sort;
import cc.oit.dao.complexQuery.WithValueQueryParam;
import cc.oit.dao.impl.mybatis.modelParser.ColumnTarget;
import cc.oit.dao.impl.mybatis.modelParser.ModelUtils;
import cc.oit.dao.impl.mybatis.modelParser.Property;
import cc.oit.model.Entity;
import cc.oit.util.ReflectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.jdbc.SQL;

import javax.persistence.Table;
import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * Created by Chanedi
 */
public class BaseSQLProvider<T extends Entity> {

    private final static Log logger = LogFactory.getLog(BaseSQLProvider.class);

	private String tableName;
	private Class<?> modelClass;
	private static ThreadLocal<Class<?>> threadModelClass = new ThreadLocal<Class<?>>();
	private static final String OPERATOR_EQUAL = " = ";
	private static final String OPERATOR_LIKE = " like ";

	private void initFromThreadLocal() {
		modelClass = BaseSQLProvider.threadModelClass.get();
		tableName = modelClass.getAnnotation(Table.class).name();

		BaseSQLProvider.threadModelClass.remove();
	}

	public static void setModelClass(Class<?> modelClass) {
		BaseSQLProvider.threadModelClass.set(modelClass);
	}

    public String getAll() {
        initFromThreadLocal();
        SQL sql = SELECT_FROM();
        sql = ORDER(sql);
        return sql.toString();
    }

    public String getById() {
        initFromThreadLocal();
        SQL sql = SELECT_FROM().WHERE("ID = #{id}");
        return sql.toString();
    }

    public String countGet(Map<String, Object> dataMap) {
        T findParams = (T) dataMap.get("findParams");
        initFromThreadLocal();
        SQL sql = COUNT_FROM();
        sql = WHERE(sql, findParams, OPERATOR_EQUAL);
        return sql.toString();
    }

    public String get(Map<String, Object> dataMap) {
        T findParams = (T) dataMap.get("findParams");
        initFromThreadLocal();
        SQL sql = SELECT_FROM();
        sql = WHERE(sql, findParams, OPERATOR_EQUAL);
        sql = ORDER(sql);
        return sql.toString();
    }

    public String countFind(Map<String, Object> dataMap) {
        T findParams = (T) dataMap.get("findParams");
        fixParamsValueToLike(findParams);
        initFromThreadLocal();

        SQL sql = COUNT_FROM();
        sql = WHERE(sql, findParams, OPERATOR_LIKE);
        return sql.toString();
    }

    public String find(Map<String, Object> dataMap) {
        T findParams = (T) dataMap.get("findParams");
        fixParamsValueToLike(findParams);
        initFromThreadLocal();

        SQL sql = SELECT_FROM();
        sql = WHERE(sql, findParams, OPERATOR_LIKE);
        sql = ORDER(sql);
        return sql.toString();
    }

    public String countQuery(Map<String, Object> customQueryParams) {
        initFromThreadLocal();
        SQL sql = COUNT_FROM();
        sql = WHERE_CUSTOM(sql, customQueryParams);
        return sql.toString();
    }

    public String query(Map<String, Object> dataMap) {
        List<Sort> sortList = (List<Sort>) dataMap.get("sortList");
        initFromThreadLocal();
        SQL sql = SELECT_FROM();
        sql = WHERE_CUSTOM(sql, dataMap);
        sql = ORDER(sql, sortList);
        return sql.toString();
    }

    public String insert(final T t) {
        initFromThreadLocal();
        // 设置默认值
        Date now = Calendar.getInstance().getTime();
//        User user = SessionUtils.getUser();
        if (StringUtils.isBlank(t.getId())) {
            t.setId(UUID.randomUUID().toString());
        }
        t.setCreateTime(now);
        t.setModifyTime(now);
//        if (user != null) {
//            t.setCreateUserCode(user.getUserCode());
//            t.setModifyUserCode(user.getUserCode());
//            try {
//                Method orgCodeGetter = t.getClass().getDeclaredMethod("getOrgCode");
//                Method orgCodeSetter = t.getClass().getDeclaredMethod("setOrgCode", String.class);
//                Object value = orgCodeGetter.invoke(t);
//                if (value == null || StringUtils.isEmpty(value.toString())) {
//                    orgCodeSetter.invoke(t, user.getOrgCode());
//                }
//            } catch (NoSuchMethodException e) {
//            }
//        } TODO

        return new SQL() {
            {
                INSERT_INTO(tableName);

                Map<String, Property> properties = ModelUtils.getProperties(t, ColumnTarget.INSERT);
                for (Property property : properties.values()) {
                    // 过滤不允许更新的字段
                    if (isIgnoreUpdate(property, t)) {
                        continue;
                    }

                    VALUES(property.getColumnName(), "#{" + property.getName() + "}");

                }
            }
        }.toString();
    }

    public String delete(String id) {
        initFromThreadLocal();
        return new SQL() {
            {
                DELETE_FROM(tableName);
                WHERE("ID = #{id}");
            }
        }.toString();
    }

    public String update(final T t) {
        initFromThreadLocal();
        // 设置默认值
        t.setModifyTime(Calendar.getInstance().getTime());
//        User user = SessionUtils.getUser();   TODO
//        if (user != null) {
//            t.setModifyUserCode(user.getUserCode());
//        }
        // 过滤不允许更新的字段
        t.setCreateTime(null);
        t.setCreateUserCode(null);

        return new SQL() {
            {
                UPDATE(tableName);

                String className = StringUtils.split(modelClass.getName(), "$")[0];
                try {
                    Map<String, Property> properties = ModelUtils.getProperties(Class.forName(className), ColumnTarget.UPDATE);

                    for (Property property : properties.values()) {
                        // 过滤不允许更新的字段
                        if (isIgnoreUpdate(property, t)) {
                            continue;
                        }

                        SET(property.getColumnName() + " = #{" + property.getName() + "}");
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
                WHERE("ID = #{id}");
            }
        }.toString();
    }

    private boolean isIgnoreUpdate(Property property, T t) {
        boolean isIgnore;
        try {
            isIgnore = property.isId() || property.isNullValue(t);
        } catch (Exception e) {
            isIgnore = true;
        }
        return isIgnore;
    }

    private void fixParamsValueToLike(T findParams) {
        PropertyDescriptor[] propertyDescriptors = ReflectUtils.getBeanSetters(findParams.getClass());
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getPropertyType() != String.class) {
                continue;
            }

            try {
                Object value = propertyDescriptor.getReadMethod().invoke(findParams);
                propertyDescriptor.getWriteMethod().invoke(findParams, "%" + value + "%");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private SQL COUNT_FROM() {
        return new SQL() {
            {
                SELECT("COUNT(ID)");
                FROM(tableName);
            }
        };
    }

    private SQL SELECT_FROM() {
        final Map<String, Property> columns = ModelUtils.getProperties(modelClass, ColumnTarget.SELECT);
        return new SQL() {
            {
                for (Property property : columns.values()) {
                    SELECT(property.getColumnName());
                }
                FROM(tableName);
            }
        };
    }

    private SQL WHERE(SQL sql, T findParams, String operator) {
        Map<String, Property> properties = ModelUtils.getProperties(findParams, ColumnTarget.WHERE);

        for (Property property : properties.values()) {
            if (operator.equalsIgnoreCase(OPERATOR_LIKE)) {
            }
            sql.WHERE(property.getColumnName() + operator + "#{findParams." + property.getName() + "}");
        }
        return sql;
    }

    private SQL WHERE_CUSTOM(SQL sql, Map<String, Object> dataMap) {
        Map<String, Property> properties = ModelUtils.getProperties(modelClass, null);
        List<CustomQueryParam> customQueryParams = (List<CustomQueryParam>) dataMap.get("queryParams");
        if (customQueryParams == null) {
            return sql;
        }
        dataMap.clear();
        int i = 0;
        for (CustomQueryParam customQueryParam : customQueryParams) {
            String key = customQueryParam.getProperty();
            Property property = properties.get(key);
            if (customQueryParam instanceof WithValueQueryParam) {
                WithValueQueryParam withValueQueryParam = (WithValueQueryParam) customQueryParam;
                dataMap.put(i + "", withValueQueryParam.getValue());
                sql.WHERE(property.getColumnName() + " " + withValueQueryParam.getOperator() + " #{" + i + "}");
                i++;
            } else if (customQueryParam instanceof  NoValueQueryParam) {
                NoValueQueryParam noValueQueryParam = (NoValueQueryParam) customQueryParam;
                sql.WHERE(property.getColumnName() + " " + noValueQueryParam.getCondition());
            }
        }
        return sql;
    }

    private SQL ORDER(SQL sql) {
        return ORDER(sql, null);
    }

    private SQL ORDER(SQL sql, List<Sort> sortList) {
        Map<String, Property> properties = ModelUtils.getProperties(modelClass, ColumnTarget.ORDER);
        for (Property property : properties.values()) {
            sql.ORDER_BY(property.getOrder());
        }
        if (sortList != null) {
            for (Sort sort : sortList) {
                sql.ORDER_BY(sort.getProperty() + " " + sort.getDirection());
            }
        }
        return sql;
    }

}
