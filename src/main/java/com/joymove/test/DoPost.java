package com.joymove.test;

import com.joymove.entity.JOYNCar;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Component("DoPost")
public class DoPost {
	
	public void doPost() throws  Exception{
		System.out.println("hello doPost");
		throw new Exception("heelo world");
	}
	public static void main(String [] args) {
		try {
			JOYNCar car = new JOYNCar();
			Map<String, Object> test = new HashMap<String, Object>();
			test.put("vinNum", "hahah");
			test.put("licensenum", "123456");
			Field[] nCar_fields = car.getClass().getFields();
			for (Field ncar_f : nCar_fields) {
				if (test.get(ncar_f.getName())!=null && String.valueOf(test.get(ncar_f.getName())).length() > 0) {
					ncar_f.setAccessible(true);
					ncar_f.set(car, test.get(ncar_f.getName()));
				}
			}
			for (Field ncar_f : nCar_fields) {
			     System.out.println("name: "+ncar_f.getName()+" value: "+ncar_f.get(car));
			}
			System.out.println(car);
		} catch (Exception e){
			e.printStackTrace();
		}
		return ;
	}
}
