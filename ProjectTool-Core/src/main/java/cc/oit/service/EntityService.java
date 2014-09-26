package cc.oit.service;

import cc.oit.dao.complexQuery.CustomQueryParam;
import cc.oit.dao.complexQuery.Sort;
import cc.oit.exception.DataCommitException;
import cc.oit.model.Entity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Chanedi
 */
public interface EntityService<T extends Entity> {

    public List<T> getAll();

    public T getById(String id);

    public int countGet(T findParams);

    public List<T> get(T findParams);

    public List<T> get(T findParams, Integer start, Integer limit);

    public int countFind(T findParams);

    public List<T> find(T findParams);

    public List<T> find(T findParams, Integer start, Integer limit);

    public int countQuery(List<CustomQueryParam> customQueryParams);

    public List<T> query(List<CustomQueryParam> customQueryParams, List<Sort> sortList);

    public List<T> query(List<CustomQueryParam> customQueryParams, List<Sort> sortList, Integer start, Integer limit);

    @Transactional(readOnly = false)
    public void insert(T t) throws DataCommitException;

    @Transactional(readOnly = false)
    public void insert(List<T> list) throws DataCommitException;

    @Transactional(readOnly = false)
    public void deleteById(Object id) throws DataCommitException;

    @Transactional(readOnly = false)
    public void deleteById(List<Object> list) throws DataCommitException;

    @Transactional(readOnly = false)
    public void delete(T t) throws DataCommitException;

    @Transactional(readOnly = false)
    public void delete(List<T> list) throws DataCommitException;

    @Transactional(readOnly = false)
    public void update(T t) throws DataCommitException;

    @Transactional(readOnly = false)
    public void update(List<T> list) throws DataCommitException;

    public void export(OutputStream outputStream, String sheetName, JSONArray columns, JSONObject queryParams);

    public List<T> findForExport(JSONObject queryParams);

}
