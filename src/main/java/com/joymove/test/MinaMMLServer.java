package com.joymove.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class MinaMMLServer {
	private static final int PORT = 9009;
	public static void main( String[] args ) throws IOException 
	{ 
	IoAcceptor acceptor = new NioSocketAcceptor(); 
	acceptor.getFilterChain().addLast( "logger", new LoggingFilter() ); 
	acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));
	acceptor.setHandler( new MinaMMLServerHandler() );
	acceptor.setDefaultLocalAddress( new InetSocketAddress(PORT) );
	acceptor.bind();
	} 
}
