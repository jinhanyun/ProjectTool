package cc.oit.dao;

import cc.oit.dao.complexQuery.CustomQueryParam;
import cc.oit.dao.complexQuery.Sort;
import cc.oit.model.Entity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by Chanedi
 */
public interface EntityDAO<T extends Entity> {

	@SelectProvider(type = BaseSQLProvider.class, method = "getAll")
	@ResultMap("getMap")
	public List<T> getAll();

	@SelectProvider(type = BaseSQLProvider.class, method = "getById")
	@ResultMap("getMap")
	public T getById(String id);

	@SelectProvider(type = BaseSQLProvider.class, method = "count")
	public int count(T findParams);

    @SelectProvider(type = BaseSQLProvider.class, method = "countQuery")
    public int countQuery(@Param("queryParams") List<CustomQueryParam> customQueryParams);
	
	@SelectProvider(type = BaseSQLProvider.class, method = "get")
	@ResultMap("getMap")
	public T getOne(T findParams);

    @SelectProvider(type = BaseSQLProvider.class, method = "query")
    @ResultMap("getMap")
    public List<T> query(@Param("queryParams") List<CustomQueryParam> customQueryParams, @Param("sortList") List<Sort> sortList);

	@SelectProvider(type = BaseSQLProvider.class, method = "get")
	@ResultMap("getMap")
	public List<T> get(T findParams);
	
	@SelectProvider(type = BaseSQLProvider.class, method = "find")
	@ResultMap("getMap")
	public List<T> find(T findParams);

	@InsertProvider(type = BaseSQLProvider.class, method = "insert")
	@Options(keyProperty = "id")
	public int insert(T t);
	
	@DeleteProvider(type = BaseSQLProvider.class, method = "delete")
	public int delete(String id);

	@UpdateProvider(type = BaseSQLProvider.class, method = "update")
	public int update(T t);

}
