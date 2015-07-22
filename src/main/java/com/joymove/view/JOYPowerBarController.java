package com.joymove.view;
import java.sql.Types;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.futuremove.cacheServer.utils.CoordinatesUtil;
import com.futuremove.cacheServer.utils.Gps;
import com.joymove.entity.*;
import com.joymove.postgres.entity.JOYPowerBar;
import org.apache.commons.collections.iterators.EntrySetMapIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.postgres.entity.*;
import com.joymove.service.JOYPowerBarService;



@Controller("JOYPowerBarController")
public class JOYPowerBarController extends  JOYBaseController {
	/*
	@Resource(name = "JOYPowerBarService")
	private  JOYPowerBarService joyPowerBarService;
    */

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;





	final static Logger logger = LoggerFactory.getLogger(JOYPowerBarController.class);


	@Transactional(propagation= Propagation.REQUIRED,rollbackFor = Exception.class)
	public  List test(String sql) throws  Exception {
		jdbcTemplate.execute("update \"JOY_PowerBar\" 12345 set id=10 where id=1;");
		throw  new Exception("ahahah");

	}

	@RequestMapping(value="rent/ttt", method=RequestMethod.POST)
	public  @ResponseBody JSONObject test(HttpServletRequest req) throws  Exception{
		System.out.println("sdfsdf");
		throw  new Exception("haha");
	}




/*
	@RequestMapping(value="rent/getNearByPowerBars", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByPowerBars(HttpServletRequest req){
		 logger.trace("getNearByPowerBars method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  pbArray  = new JSONArray();
		
		 Reobj.put("result", "10001");
		 Reobj.put("powerbars", pbArray);
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");;
			 likeCondition.put("userPositionX", jsonObj.get("userLongitude")==null ? 0.0: jsonObj.get("userLongitude") );
			 likeCondition.put("userPositionY", jsonObj.get("userLatitude")==null ? 0.0: jsonObj.get("userLatitude") );
			 likeCondition.put("scope", jsonObj.get("scope")==null ? 10 : jsonObj.get("scope") );
			 List<JOYPowerBar> pbs = joyPowerBarService.getPowerBarByScope(likeCondition);
			 
			 Iterator iter = pbs.iterator();
			 while(iter.hasNext()){
				 JOYPowerBar pb_item  = (JOYPowerBar)iter.next();
				 JSONObject pb_json = new JSONObject();
				 pb_json.put("powerbarId", pb_item.id);
				 pb_json.put("longitude",  pb_item.positionX);
				 pb_json.put("latitude",  pb_item.positionY);
				 pb_json.put("desp",  pb_item.desp);
				 pbArray.add(pb_json);
			 }
			 Reobj.put("result", "10000");
			 
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 logger.error(e.getStackTrace().toString());
		 }
		 return Reobj;
	}
	*/
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ) // 增加这个事务是为了防止两次读取不一致
	@RequestMapping(value="rent/getNearByPowerBars", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByPowerBars(HttpServletRequest req){
		logger.trace("getNearByPowerBars method was invoked...");
		Map<String,Object> likeCondition = new HashMap<String, Object>();
		JSONObject Reobj=new JSONObject();
		JSONArray  pbArray  = new JSONArray();

		Reobj.put("result", "10001");
		Reobj.put("powerbars", pbArray);
		//先是处理输入
		Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
		Double userLongitude = jsonObj.get("userLongitude")==null ? 0.0: Double.parseDouble(String.valueOf(jsonObj.get("userLongitude")));
		Double userLatitude = jsonObj.get("userLatitude")==null ? 0.0: Double.parseDouble(String.valueOf(jsonObj.get("userLatitude")));
		Long scope =   jsonObj.get("scope")==null ? 10L : Long.parseLong(String.valueOf(jsonObj.get("scope")));
		Long start =   jsonObj.get("start")==null ? 0L : Long.parseLong(String.valueOf(jsonObj.get("start")));
		if(start>9000) start =9000L;
		Long limit =   jsonObj.get("limit")==null ? 20L : Long.parseLong(String.valueOf(jsonObj.get("limit")));
		if(limit>10000L) limit=10000L;
		Gps userWgs84 = CoordinatesUtil.gcj02_To_Gps84(userLatitude, userLongitude);
		//开始构建SQL
		/*
		String querySQL = "SELECT id,ST_AsText(location) as location,extendinfo,desp,\"testArr\" from \"JOY_PowerBar\" where ST_DWithin(location,ST_GeographyFromText(\'POINT("
				+userLongitude+" "
				+userLatitude+")\'),"+scope+");";
		*/
		String queryCountSQL = "SELECT count(id) from \"JOY_PowerBar\" where (location <-> ST_GeomFromText(\'POINT("
				+userLongitude+" "
				+userLatitude+")\',4326)) < "+scope+"/111045::float limit 10000;";

		System.out.println("queryCountSQL is "+queryCountSQL);
		long qCount = jdbcTemplate.queryForLong(queryCountSQL);
		if(qCount>0) {
			Reobj.put("count",qCount);
			if(limit>qCount) limit = qCount;
			String querySQL = "SELECT id,ST_AsText(location) as location,extendinfo,desp,\"testArr\" from \"JOY_PowerBar\" ORDER BY location <-> ST_GeomFromText(\'POINT("
					+userLongitude+" "
					+userLatitude+")\',4326) limit "+limit +" offset "+start +";";
			List items = jdbcTemplate.query(querySQL,
					new JOYRowMapper(JOYPowerBar.class));
			long sizeTop = (qCount - start)>0? (qCount - start) : 0;
			if(sizeTop > items.size()) sizeTop = items.size();
			for(int i=0;i<sizeTop;i++) {
				JOYPowerBar pb = (JOYPowerBar)items.get(i);
				pbArray.add(JOYPowerBar.toJSON(pb));
			}
			Reobj.put("result", "10000");
		} else {
			Reobj.put("count",0);
			Reobj.put("result","10002");
		}




		return Reobj;
	}

	public static void main(String[] args) throws  Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:**/applicationContext-mvc.xml");
		JOYPowerBarController tempalte = (JOYPowerBarController)context.getBean("JOYPowerBarController");
		System.out.println("hello, i am here\n");
		List<Map<String, Object>> list = tempalte.test(" SELECT * from \"JOY_PowerBar\" where location = ST_GeomFromText('SRID=4326;POINT(3.2 2.4)') and \"extendinfo\" = '{\"q\":\"r\"}';");
		System.out.println("get the result,hha");
		for(int i=0;i<list.size();i++) {
			System.out.println(i+" th line content:");
		    Map<String,Object> map = list.get(i);
			Set<Map.Entry<String, Object>> entrySet =  map.entrySet();
			System.out.println("{");
			Iterator iter = entrySet.iterator();
			while(iter.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>)iter.next();
				System.out.println("\t "+entry.getKey()+" : "+entry.getValue());
			}
			System.out.println("}");
		}
	}

}
