package edu.asu.commons.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import edu.asu.commons.event.Event;
import edu.asu.commons.event.EventChannel;
import edu.asu.commons.event.EventTypeProcessor;
import edu.asu.commons.net.AbstractDispatcher;
import edu.asu.commons.net.ClientDispatcher;
import edu.asu.commons.net.Identifier;
import edu.asu.commons.net.SocketIdentifier;
import edu.asu.commons.net.event.DisconnectionRequest;

public class ClientNettyDispatcher extends AbstractDispatcher implements ClientDispatcher {

    private Identifier id;
    private Channel channel;

    public ClientNettyDispatcher(EventChannel channel) {
        super(channel);
        channel.add(this, new EventTypeProcessor<DisconnectionRequest>(DisconnectionRequest.class) {
            public void handle(DisconnectionRequest event) {
                disconnect(event.getId());
            }
        });
    }

    @Override
    public void disconnect(Identifier id) {
        channel.close();
    }

    @Override
    public void transmit(Event event) {
        channel.write(event);
    }

    @Override
    public void shutdown() {
        channel.close();
    }

    @Override
    public Identifier connect(InetSocketAddress remoteAddress) {
        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                        new ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(Thread.currentThread().getContextClassLoader())),
                        new ObjectEncoder(),
                        new ClientEventChannelHandler());
            }
        });
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.connect(remoteAddress);
        InetSocketAddress localAddress = (InetSocketAddress) bootstrap.getOption("localAddress");
        id = new SocketIdentifier(remoteAddress, localAddress);
        return id;
    }

    private class ClientEventChannelHandler extends SimpleChannelHandler {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            Event event = (Event) e.getMessage();
            getLocalEventHandler().handle(event);
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
            channel = e.getChannel();
            id = new SocketIdentifier((InetSocketAddress) channel.getLocalAddress(), (InetSocketAddress) channel.getRemoteAddress());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getCause().printStackTrace();
            requestDisconnection(id, e.getCause());
        }
    }

}
