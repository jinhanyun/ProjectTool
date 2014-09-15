package cc.oit.service;

import cc.oit.dao.EntityDAO;
import cc.oit.dao.SqlInterceptor;
import cc.oit.dao.complexQuery.CustomQueryParam;
import cc.oit.dao.complexQuery.Sort;
import cc.oit.exception.DataCommitException;
import cc.oit.model.Entity;
import cc.oit.util.DateUtils;
import cc.oit.util.ReflectUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Chanedi
 */
@Transactional(rollbackFor = { Exception.class }, readOnly = true)
public abstract class EntityServiceImpl<T extends Entity> implements EntityService<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private EntityDAO<T> entityDAO;

	@Override
	public List<T> getAll() {
		return entityDAO.getAll();
	}

	@Override
	public T getById(String id) {
		return entityDAO.getById(id);
	}

	@Override
	public int count(T findParams) {
		return entityDAO.count(findParams);
	}

    @Override
    public int countQuery(List<CustomQueryParam> customQueryParams) {
        return entityDAO.countQuery(customQueryParams);
    }

    @Override
    public List<T> query(List<CustomQueryParam> customQueryParams) {
        return entityDAO.query(customQueryParams, null);
    }

    @Override
    public List<T> query(List<CustomQueryParam> customQueryParams, Integer start, Integer limit, List<Sort> sortList) {
        if (start != null && limit != null) {
            SqlInterceptor.setRowBounds(new RowBounds(start, limit));
        }
        return entityDAO.query(customQueryParams, sortList);
    }

    @Override
    public List<T> getByObj(T findParams) {
        return entityDAO.get(findParams);
    }
    
	@Override
	public List<T> find(T findParams, int start, int limit) {
		SqlInterceptor.setRowBounds(new RowBounds(start, limit));
		return entityDAO.find(findParams);
	}

	@Override
	@Transactional(readOnly = false)
	public void insert(T t) {
		if (entityDAO.insert(t) != 1) {
			throw new DataCommitException();
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void insert(List<T> list) {
		for (T t : list) {
			if (entityDAO.insert(t) != 1) {
				throw new DataCommitException();
			}
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteById(String id) {
		if (entityDAO.delete(id) != 1) {
			throw new DataCommitException();
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void deleteById(List<String> list) {
		for (String id : list) {
			if (entityDAO.delete(id) != 1) {
				throw new DataCommitException();
			}
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void update(T t) {
		if (entityDAO.update(t) != 1) {
			throw new DataCommitException();
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void update(List<T> list) {
		for (T t : list) {
			if (entityDAO.update(t) != 1) {
				throw new DataCommitException();
			}
		}
	}

    @Override
    public void export(OutputStream outputStream, String sheetName, JSONArray columns,JSONObject queryFilter) {
        WritableWorkbook wwb = null;
        try {
            wwb = Workbook.createWorkbook(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        WritableSheet sheet = wwb.createSheet(sheetName, 0);
        for (int i = 0; i < columns.size(); i++) {
            JSONObject jsonObject = (JSONObject) columns.get(i);
            try {
                sheet.addCell(new Label(i, 0, jsonObject.getString("text")));
            } catch (WriteException e) {
                throw new RuntimeException(e);
            }
        }

        List<T> list = findForExport(queryFilter);
        if (list.size() == 0) {
            try {
                wwb.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        }

        Class<?> modelClass = list.get(0).getClass();
        List<Method> readMethods = new ArrayList<Method>();
        for (int i = 0; i < columns.size(); i++) {
            JSONObject jsonObject = (JSONObject) columns.get(i);
            String prop = (String) jsonObject.get("dataIndex");
            try {
                Method readMethod = ReflectUtils.getBeanGetter(modelClass, prop);
                readMethods.add(readMethod);
            } catch (IntrospectionException e) {
                readMethods.add(null);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < readMethods.size(); j++) {
                T obj = list.get(i);

                Method readMethod = readMethods.get(j);
                String valueStr = null;
                if (readMethod != null) {
                    Object value = null;
                    try {
                        value = readMethod.invoke(obj);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    valueStr = value == null ? "" : value.toString();

                    if (value instanceof Date) {
                        DateTimeFormat dateTimeFormat = readMethod.getAnnotation(DateTimeFormat.class);
                        DateFormat df;
                        if (dateTimeFormat == null) {
                            try {
                                Field field = ReflectUtils.getFieldByGetter(modelClass, readMethod.getName());
                                dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
                            } catch (NoSuchFieldException e) {
                            }
                        }
                        if (dateTimeFormat == null) {
                            df = new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT);
                        } else {
                            df = new SimpleDateFormat(dateTimeFormat.pattern());
                        }
                        valueStr = df.format(value);
                    }
                }
                try {
                    sheet.addCell(new Label(j, i + 1, valueStr));
                } catch (WriteException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            wwb.write();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            try {
                wwb.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<T> findForExport(JSONObject queryParams) {
        return entityDAO.getAll();
    }

}
