package com.joymove.test.testMina;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.write.WriteRequest;
import  org.apache.mina.core.write.DefaultWriteRequest;
import  org.apache.mina.core.buffer.IoBuffer;
import java.lang.Thread;

public class TestFilter1  extends IoFilterAdapter {
	 @Override
	    public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
	        //logger.trace("filter 1 sOpen start ");
	        nextFilter.sessionCreated(session);
	        //logger.trace("filter 1 sOpen end ");
	        // Some other logic here...
	    }
	 public void messageReceived(IoFilter.NextFilter nextFilter,
             IoSession session,
             Object message)
               throws Exception {
			Thread thread = Thread.currentThread();
			long thread_id = thread.getId();
		//	logger.trace("filter 1 thread id "+thread_id);
		 
		// logger.trace("filter 1 mr start ");
	        nextFilter.messageReceived(session,message);
	    //    logger.trace("filter 1  mr   end ");
		 
	 }
               
	 @Override
	 public void messageSent(IoFilter.NextFilter nextFilter,IoSession session, WriteRequest writeRequest){
		 //  logger.trace("filter 1 ms start ");
			Thread thread = Thread.currentThread();
			long thread_id = thread.getId();
			//logger.trace("filter 1 thread id "+thread_id);
	        nextFilter.messageSent(session,writeRequest);
	       // logger.trace("filter 1  ms end ");
	 }
	 
	 @Override
	  public void filterWrite(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest){
		   // logger.trace("filter 1 fw start ");
		    String message = ((String) writeRequest.getMessage());
		    //the last write to the IOAccpetor or IOConnector must be IoBuffer
		    IoBuffer buf  = IoBuffer.allocate(200);
		    buf.putObject(message+"\n");
		    //logger.trace("the message is "+message);
	        nextFilter.filterWrite(session, new DefaultWriteRequest(
	                buf, writeRequest.getFuture(), writeRequest.getDestination()));
	        //logger.trace("filter 1 fw end ");
	        // Some other logic here...
	  }
  
}
