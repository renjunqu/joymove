package com.joymove.test.testMina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.executor.ExecutorFilter;

public class TestServer {
	private static final int PORT = 9009;
	public static void main( String[] args ) throws IOException 
	{ 
	IoAcceptor acceptor = new NioSocketAcceptor(); 
	
	//acceptor.getFilterChain().addLast( "logger", new LoggingFilter() ); 
	acceptor.getFilterChain().addLast( "codec", new TestFilter1() );
	acceptor.getFilterChain().addLast( "executor", new TestFilter2() );
	
	acceptor.getFilterChain().addLast( "filter2", new ExecutorFilter(10) ); 
	acceptor.getFilterChain().addLast( "filter3", new TestFilter3() ); 
	
	//acceptor.getFilterChain().addLast( "filter2", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));
	acceptor.setHandler( new TestHandler() );
	acceptor.setDefaultLocalAddress( new InetSocketAddress(PORT) );
	acceptor.bind();
	} 
}