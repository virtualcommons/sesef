package edu.asu.commons.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.net.AbstractServerDispatcher;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.net.SocketIdentifier;

public class NettyDispatcher extends AbstractServerDispatcher {
    
    private Logger logger = Logger.getLogger(NettyDispatcher.class.getName());

    // a mapping between Netty's channel ids to our Identifiers
    private final Map<Integer, Identifier> channelIdentifiers = new HashMap<Integer, Identifier>();
    private final Map<Identifier, Channel> channels = new HashMap<Identifier, Channel>();

    public NettyDispatcher(EventChannel channel) {
        super(channel);
    }

    @Override
    public boolean isConnected(Identifier id) {
        return channels.containsKey(id);
    }

    @Override
    public void disconnect(Identifier id) {
        channels.remove(id);
    }

    @Override
    public void transmit(Event event) {
        Channel channel = channels.get(event.getId());
        ChannelFuture future = channel.write(event);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.info("Completed write: " + future);
            }
        });
    }

    @Override
    protected void bind(int port) throws IOException {
        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(
                        new ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(Thread.currentThread().getContextClassLoader())),
                        new ObjectEncoder(),
                        new ServerEventChannelHandler());
            }
        });
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.bind(new InetSocketAddress(port));
    }

    @Override
    protected void processIncomingConnections() throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void cleanup() {
        // TODO Auto-generated method stub

    }
    
    private class ServerEventChannelHandler extends SimpleChannelHandler {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            Event event = (Event) e.getMessage();
            getLocalEventHandler().handle(event);
        }
        
        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
            Channel channel = e.getChannel();
            Identifier id = new SocketIdentifier((InetSocketAddress) channel.getLocalAddress(), (InetSocketAddress) channel.getRemoteAddress());
            channelIdentifiers.put(channel.getId(), id);
            channels.put(id, channel);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getCause().printStackTrace();
            Channel ch = e.getChannel();
            requestDisconnection(channelIdentifiers.get(ch.getId()), e.getCause());
        }
    }

}
