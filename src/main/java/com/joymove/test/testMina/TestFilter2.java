package com.joymove.test.testMina;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.write.WriteRequest;

public class TestFilter2  extends IoFilterAdapter {
	 @Override
	    public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
	       // logger.trace("filter 2 sOpen start ");
	        nextFilter.sessionCreated(session);
	      //  logger.trace("filter 2 sOpen end ");
	        // Some other logic here...
	    }
	 
	 
	 public void messageReceived(IoFilter.NextFilter nextFilter,
             IoSession session,
             Object message)
               throws Exception {
		 Thread thread = Thread.currentThread();
			long thread_id = thread.getId();
		//	logger.trace("filter 2 thread id "+thread_id);
		 
		// logger.trace("filter 1 mr start ");
	        nextFilter.messageReceived(session,message);
	    //    logger.trace("filter 1  mr   end ");
		 
	 } 
	 @Override
	 public void messageSent(IoFilter.NextFilter nextFilter,IoSession session, WriteRequest writeRequest){
		  // logger.trace("filter 2 ms start ");
	        nextFilter.messageSent(session,writeRequest);
	      //  logger.trace("filter 2  ms end ");
	 }
	 @Override
	  public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest){
		  //  logger.trace("filter 2 fw start ");
	        nextFilter.filterWrite(session,writeRequest);
	      //  logger.trace("filter 2 fw end ");
	        // Some other logic here...
	  }
}

