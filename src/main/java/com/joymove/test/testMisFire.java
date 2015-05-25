package com.joymove.test;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

public class testMisFire {
	
	public static void main(String [] args){
		 Scheduler scheduler = null;
		 
		try {
			JobKey key = new JobKey("1850021764222222222222222222", "clearExpire");
			
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.deleteJob(key);
		       // and start it off
		      // scheduler.start();
	       System.out.println("now show instance id ");
			System.out.println(scheduler.getSchedulerInstanceId());
	    // enumerate each job group
			System.out.println("now  show jobs:  ");
	       for(String group: scheduler.getJobGroupNames()) {
	           // enumerate each job in group
	    	   System.out.println("job of group "+ group);   
	           for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))){
	               System.out.println("Found job identified by: " + jobKey);
	           }
	       }
	       System.out.println("showing over");
	       
	       //scheduler.
	  } catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
