package cc.oit.service;

import cc.oit.dao.complexQuery.CustomQueryParam;
import cc.oit.dao.complexQuery.Sort;
import cc.oit.exception.DataCommitException;
import cc.oit.model.Entity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

	public int count(T findParams);

    public int countQuery(List<CustomQueryParam> customQueryParams);

    public List<T> query(List<CustomQueryParam> customQueryParams);

    public List<T> query(List<CustomQueryParam> customQueryParams, Integer start, Integer limit, List<Sort> sortList);

    public List<T> getByObj(T findParams) ;

	public List<T> find(T findParams, int start, int limit);

	public void insert(T t);
	
	public void insert(List<T> list);

	public void deleteById(String id);
	
	public void deleteById(List<String> list);

	public void update(T t);
	
	public void update(List<T> list);

    public void export(OutputStream outputStream, String sheetName, JSONArray columns,JSONObject queryFilter);

    public List<T> findForExport(JSONObject jsonParams);

}
