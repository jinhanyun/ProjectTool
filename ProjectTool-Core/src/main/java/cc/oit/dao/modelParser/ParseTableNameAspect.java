package cc.oit.dao.modelParser;

import cc.oit.dao.BaseSQLProvider;
import org.apache.ibatis.binding.MapperProxy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * Created by Chanedi
 */
@Aspect
@Component
public class ParseTableNameAspect {

	@Around("execution(* cc.oit.dao.EntityDAO.*(..))")
	public Object invoke(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {
		parseTableName(proceedingJoinPoint);
		
		return proceedingJoinPoint.proceed();
	}
	
	private void parseTableName(ProceedingJoinPoint proceedingJoinPoint)
			throws NoSuchFieldException, IllegalAccessException,
			ClassNotFoundException {
		// 获取代理目标对象
		Field h = Proxy.class.getDeclaredField("h");
		h.setAccessible(true);
		Object proxyTarget = h.get(proceedingJoinPoint.getTarget());

		// 获取dao类
		Field mapperInterface = MapperProxy.class
				.getDeclaredField("mapperInterface");
		mapperInterface.setAccessible(true);
		Class<?> cl = (Class<?>) mapperInterface.get(proxyTarget);
		
		// 获取model类
		String modelName = cl.getName().replace(".dao.", ".model.").replace("DAO", "");

		// 将modelClass添加到线程变量
		BaseSQLProvider.setModelClass(Class.forName(modelName));
	}

}
