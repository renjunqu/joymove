package com.joymove.test.testMina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.write.WriteRequest;

public class TestFilter3  extends IoFilterAdapter {
	 @Override
	    public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
	     //   System.out.println("filter 3 sOpen start ");
	        nextFilter.sessionCreated(session);
	     //   System.out.println("filter 3 sOpen end ");
	        // Some other logic here...
	    }
	 
	 public void messageReceived(IoFilter.NextFilter nextFilter,
             IoSession session,
             Object message)
               throws Exception {
		 Thread thread = Thread.currentThread();
			long thread_id = thread.getId();
			System.out.println("filter 3 thread id "+thread_id);
		 
		// System.out.println("filter 1 mr start ");
	        nextFilter.messageReceived(session,message);
	    //    System.out.println("filter 1  mr   end ");
	 }
	 
	 @Override
	 public void messageSent(IoFilter.NextFilter nextFilter,IoSession session, WriteRequest writeRequest){
		//   System.out.println("filter 3 ms start ");
	        nextFilter.messageSent(session,writeRequest);
	     //   System.out.println("filter 3  ms end ");
	 }
	 
	 @Override
	  public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest){
		 //   System.out.println("filter 3 fw start ");
	        nextFilter.filterWrite(session,writeRequest);
	        //writeRequest.getMessage();
	     //   System.out.println("filter 3 fw end ");
	        // Some other logic here...
	  }
  
}
