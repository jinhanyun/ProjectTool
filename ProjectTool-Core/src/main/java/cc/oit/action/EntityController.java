package cc.oit.action;

import cc.oit.action.view.ComparisonTranslator;
import cc.oit.action.view.TableView;
import cc.oit.action.view.UpdateResult;
import cc.oit.context.ContextUtils;
import cc.oit.dao.complexQuery.CustomQueryParam;
import cc.oit.dao.complexQuery.Sort;
import cc.oit.dao.complexQuery.WithValueQueryParam;
import cc.oit.exception.MessageException;
import cc.oit.model.Entity;
import cc.oit.service.EntityService;
import cc.oit.util.ReflectUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/{moduleName}/{submoduleName}")
public class EntityController {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(produces="text/html")
    public String index(@PathVariable String moduleName, @PathVariable String submoduleName, Model model) {
	    model.addAttribute("moduleName", moduleName);
        model.addAttribute("submoduleName", submoduleName);
        return moduleName + "." + submoduleName; 
    }
	
    @RequestMapping
    @ResponseBody
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TableView list(HttpServletRequest request, @PathVariable String submoduleName, @RequestParam(required = false) String sort,
    		@RequestParam(required = false) Integer start, @RequestParam(required = false) Integer limit) {
    	EntityService<Entity> entityService = (EntityService) ContextUtils.getBean(submoduleName + "ServiceImpl");
    	// 构建findParams
    	String serviceClassName = entityService.toString().split("@")[0];
    	String modelClassName = serviceClassName.replace("service.impl.", "model.").replace("ServiceImpl", "");
        Class<?> modelClass = null;
        try {
            modelClass = Class.forName(modelClassName);
        } catch (ClassNotFoundException e) {
            // TODO 404
            return null;
        }

        // 设置findParams属性
        List<CustomQueryParam> queryParams = new ArrayList<CustomQueryParam>();
		Map<String, String[]> parameterMap = request.getParameterMap();
		PropertyDescriptor[] propDescriptors = ReflectUtils.getBeanGetters(modelClass);
    	for (PropertyDescriptor propDescriptor : propDescriptors) {
            String propName = propDescriptor.getName();
            String[] values = parameterMap.get(propName);
    		if (values == null || values.length == 0) { // 不存在的key
    			continue;
    		}

    		String value = values[0];
    		if (StringUtils.isEmpty(value.toString())) { // 空值
    			continue;
    		}
            try {
                queryParams.add(new WithValueQueryParam(propName, "LIKE", "%" + URLDecoder.decode(value, "utf-8") + "%"));
            } catch (UnsupportedEncodingException e) {
            }
        }

        // 根据filter设置findParams属性
        addFilterQueryParams(request, queryParams);

        // 查询
    	List<?> list = entityService.query(queryParams, JSONArray.parseArray(sort, Sort.class), start, limit);
    	TableView tableView = new TableView();
    	tableView.setRows(list);
        tableView.setTotal(entityService.countQuery(queryParams));
    	return tableView;
    }

    public static void addFilterQueryParams(HttpServletRequest request, List<CustomQueryParam> queryParams) {
        for (int i = 0; ; i++) {
            String prefix = "filter[" + i + "]";
            String field = request.getParameter(prefix + "[field]");
            if (field == null) {
                break;
            }
            Object value = request.getParameter(prefix + "[data][value]");
            String type = request.getParameter(prefix + "[data][type]");
            String comparison = request.getParameter(prefix + "[data][comparison]");
            if ("string".equals(type)) {
                queryParams.add(new WithValueQueryParam(field, "LIKE", "%" + value + "%"));
            } else if (comparison != null) {
                if ("date".equals(type)) {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        value = df.parse(value.toString());
                    } catch (ParseException e) {
                        throw new MessageException("日期格式不正确");
                    }
                }
                queryParams.add(new WithValueQueryParam(field, ComparisonTranslator.translate(comparison), value));
            } else {
                queryParams.add(new WithValueQueryParam(field, "=", value));
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public UpdateResult save(@PathVariable String moduleName, @PathVariable String submoduleName, @RequestBody String jsonText)  {
		EntityService<Entity> entityService = getService(submoduleName);
		UpdateResult updateResult = new UpdateResult();

        if (jsonText.startsWith("[")) {
            List<Entity> objects = parseModels(jsonText, moduleName, submoduleName);
            entityService.insert(objects);
            updateResult.addResult(objects);
        } else {
            Entity object = parseModel(jsonText, moduleName, submoduleName);
            entityService.insert(object);
            updateResult.addResult(object);
        }

		return updateResult;
	}


    @RequestMapping(value="/{id}", method = RequestMethod.PUT, consumes="application/json")
	@ResponseBody
	public UpdateResult update(@PathVariable String moduleName,@PathVariable String submoduleName, @RequestBody String jsonText)  {
		EntityService<Entity> entityService = getService(submoduleName);
		UpdateResult updateResult = new UpdateResult();

        if (jsonText.startsWith("[")) {
            List<Entity> objects = parseModels(jsonText, moduleName, submoduleName);
            entityService.update(objects);
            updateResult.addResult(objects);
        } else {
            Entity object = parseModel(jsonText,moduleName, submoduleName);
            entityService.update(object);
            updateResult.addResult(object);
        }

		return updateResult;
	}

	@RequestMapping(value="/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public UpdateResult delete(@PathVariable String submoduleName, @PathVariable String id, @RequestBody String jsonText) {
		EntityService<Entity> entityService = getService(submoduleName);
		UpdateResult updateResult = new UpdateResult();

		if (jsonText.startsWith("[")) {
			// 删除多条记录
			JSONArray jsonArray = JSON.parseArray(jsonText);
			List<String> list = new ArrayList<String>();
			for (Object object : jsonArray) {
				JSONObject jsonObject = (JSONObject) object;
				list.add((String) jsonObject.get("id"));
			}
			entityService.deleteById(list);
		} else {
			entityService.deleteById(id);
		}

		return updateResult;
	}

	@RequestMapping(value="/export/{fileName}", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject export(HttpServletRequest request,
                       HttpServletResponse response,
                       @PathVariable String submoduleName,
                       @PathVariable String fileName,
                       @RequestParam String params,
                       @RequestParam(required = false) String queryParams) {
//		EntityService<Entity> entityService = getService(submoduleName); TODO
//        JSONObject queryFilter  = JSONObject.parseObject(queryParams);
//        int countExportRows = entityService.countForExport(queryFilter);
//        Integer maxExportLine = Integer.parseInt(WebContextUtils.getPropValue(WebConstants.MAX_EXPORT_LINE));
//        if(countExportRows > maxExportLine){
//            JSONObject result = new JSONObject();
//            result.put("msg","export rows is than maxExportLine");
//            return result;
//        }
//        JSONArray columns = JSONArray.parseArray(params);
//        String sheetName = fileName;
//        fileName = URLEncoder.encode(fileName, "UTF8") + ".xls";
//        String userAgent = request.getHeader("User-Agent").toLowerCase(); // 获取用户代理
//        if (userAgent.indexOf("msie") != -1) { // IE浏览器
//            fileName = "filename=\"" + fileName + "\"";
//        } else if (userAgent.indexOf("mozilla") != -1) { // firefox浏览器
//            fileName = "filename*=UTF-8''" + fileName;
//        }
//        response.reset();
//        response.setContentType("application/ms-excel");
//        response.setHeader("Content-Disposition", "attachment;" + fileName);
//        OutputStream os = response.getOutputStream();
//        entityService.export(os, sheetName, columns, queryFilter);
//        os.close();
        return null;
	}

	@SuppressWarnings("unchecked")
	private EntityService<Entity> getService(String submoduleName) {
		return (EntityService<Entity>) ContextUtils.getBean(submoduleName + "ServiceImpl");
	}

	private Entity parseModel(String jsonText, String moduleName, String submoduleName)  {
        try {
            return (Entity) JSON.parseObject(jsonText, reflectModel(moduleName, submoduleName));
        } catch (ClassNotFoundException e) {
            // TODO 404
            return null;
        }
    }

    private List<Entity> parseModels(String jsonText, String moduleName, String submoduleName)  {
        try {
            return (List<Entity>) JSON.parseArray(jsonText, reflectModel(moduleName, submoduleName));
        } catch (ClassNotFoundException e) {
            // TODO 404
            return null;
        }
    }

    private Class reflectModel(String moduleName, String submoduleName) throws ClassNotFoundException {
        String modelClassName = "cc.oit.bsmes." + moduleName + ".model." + StringUtils.capitalize(submoduleName);
        return Class.forName(modelClassName);
    }
}
