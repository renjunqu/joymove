package com.joymove.test;

import java.util.Date;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.futuremove.cacheServer.jobs.ClearExpireReserveJob;
import com.joymove.service.impl.JOYNReserveOrderServiceImpl;

import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;
import com.futuremove.cacheServer.test.quartz.HelloJob;
import com.futuremove.cacheServer.jobs.ClearExpireReserveJob;

public class quartzLister {
	
	final static Logger logger = LoggerFactory.getLogger(JOYNReserveOrderServiceImpl.class);
	
	
	public static void main(String[] args){
		System.out.println("now, lest's start");
		 // Grab the Scheduler instance from the Factory 
		//ClearExpireReserveJob.testMain();
	try {
       
  
  } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}

}
