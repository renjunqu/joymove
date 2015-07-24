package com.joymove.view;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.futuremove.cacheServer.utils.CoordinatesUtil;
import com.futuremove.cacheServer.utils.Gps;
import com.joymove.postgres.entity.JOYPowerBar;
import com.joymove.postgres.entity.JOYRowMapper;
import org.apache.velocity.Template;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mybatis.scripting.velocity.VelocityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.joymove.service.JOYParkService;
import com.joymove.entity.JOYPark;



@Controller("JOYParkController")
public class JOYParkController {

	/*
	@Resource(name = "JOYParkService")
	private  JOYParkService joyParkService;
    */

	public static String queryNearyByCount = "SELECT count(id) from \"JOY_Park\" " +
			"where (location <-> ST_GeomFromText('POINT(${longitude} ${latitude})',4326)) < ${scope}/111045::float limit 10000;";
	public static String queryNearyByParks = "SELECT id,ST_AsText(location) as location,extendinfo,desp from \"JOY_Park\" " +
			"ORDER BY location <-> ST_GeomFromText('POINT(${longitude} ${latitude})',4326) limit ${limit} offset ${start} ;";



	public static Template countTemplate;
	public static Template parkTemplate;

	{
		//反正是单例模式，就放在实例的初始化块中好了。
		Template countTemplate = (Template) VelocityFacade.compile(JOYParkController.queryNearyByCount, "queryCount");
		Template parkTemplate = (Template) VelocityFacade.compile(JOYParkController.queryNearyByParks, "queryParks");
	}




	final static Logger logger = LoggerFactory.getLogger(JOYParkController.class);

	@Resource(name="jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Transactional(propagation= Propagation.REQUIRED,rollbackFor = Exception.class,isolation = Isolation.REPEATABLE_READ) // 增加这个事务是为了防止两次读取不一致
	@RequestMapping(value="rent/getNearByParks", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByPowerBars(HttpServletRequest req){
		logger.trace("getNearByParks method was invoked...");
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
		//开始构建SQL
		Map<String,Object> sqlContext = new HashMap<String,Object>();
		sqlContext.put("longitude",userLongitude);
		sqlContext.put("latitude",userLatitude);
		sqlContext.put("scope",scope);
		sqlContext.put("start",start);
		sqlContext.put("limit",limit);
		String queryCountSQL = VelocityFacade.apply(countTemplate, sqlContext);

		/*
		String querySQL = "SELECT id,ST_AsText(location) as location,extendinfo,desp,\"testArr\" from \"JOY_PowerBar\" where ST_DWithin(location,ST_GeographyFromText(\'POINT("
				+userLongitude+" "
				+userLatitude+")\'),"+scope+");";

		String queryCountSQL = "SELECT count(id) from \"JOY_Park\" where (location <-> ST_GeomFromText(\'POINT("
				+userLongitude+" "
				+userLatitude+")\',4326)) < "+scope+"/111045::float limit 10000;";
		*/

		System.out.println("queryCountSQL is " + queryCountSQL);
		long qCount = jdbcTemplate.queryForLong(queryCountSQL);
		if(qCount>0) {
			Reobj.put("count",qCount);
			if(limit>qCount) limit = qCount;
			String querySQL = VelocityFacade.apply(parkTemplate, sqlContext);
			/*
			String querySQL = "SELECT id,ST_AsText(location) as location,extendinfo,desp from \"JOY_Park\" ORDER BY location <-> ST_GeomFromText(\'POINT("
					+userLongitude+" "
					+userLatitude+")\',4326) limit "+limit +" offset "+start +";";
			*/

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




	/*
	@RequestMapping(value="rent/getNearByParks", method=RequestMethod.POST)
	public  @ResponseBody JSONObject getNearByParks(HttpServletRequest req){
		 logger.trace("getNearByParks method was invoked...");
		 Map<String,Object> likeCondition = new HashMap<String, Object>();
		 JSONObject Reobj=new JSONObject();
		 JSONArray  parkArray  = new JSONArray();
		
		 Reobj.put("result", "10001");
		 Reobj.put("parks", parkArray);
		 try {
			 Hashtable<String, Object> jsonObj = (Hashtable<String, Object>)req.getAttribute("jsonArgs");
			 likeCondition.put("userPositionX", jsonObj.get("userLongitude")==null ? 0.0: jsonObj.get("userLongitude") );
			 likeCondition.put("userPositionY", jsonObj.get("userLatitude")==null ? 0.0: jsonObj.get("userLatitude") );
			 likeCondition.put("scope", jsonObj.get("scope")==null ? 10 : jsonObj.get("scope") );
			 List<JOYPark> parks = joyParkService.getParkByScope(likeCondition);
			 
			 Iterator iter = parks.iterator();
			 while(iter.hasNext()){
				 JOYPark park_item  = (JOYPark)iter.next();
				 JSONObject park_json = new JSONObject();
				 park_json.put("parkId", park_item.id);
				 park_json.put("longitude",  park_item.positionX);
				 park_json.put("latitude",  park_item.positionY);
				 park_json.put("desp",  park_item.desp);
				
				 parkArray.add(park_json);
			 }
			 Reobj.put("result", "10000");
			 
		 } catch(Exception e) {
			 Reobj.put("result", "10001");
			 logger.error(e.getStackTrace().toString());
		 }
		 return Reobj;
	}
	*/

}
