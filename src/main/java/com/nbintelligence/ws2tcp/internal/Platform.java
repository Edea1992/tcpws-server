package com.nbintelligence.ws2tcp.internal;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ThreadFactory;

public final class Platform {
    public final static String PREFERRED;
    public final static Class<? extends EventLoopGroup> PREFERRED_EVENT_LOOP_GROUP_CLASS;
    public final static Class<? extends ServerSocketChannel> PREFERRED_SERVER_SOCKET_CHANNEL_CLASS;
    public final static Class<? extends SocketChannel> PREFERRED_SOCKET_CHANNEL_CLASS;

    static {
        if (Epoll.isAvailable()) {
            PREFERRED = "epoll";
            PREFERRED_EVENT_LOOP_GROUP_CLASS = EpollEventLoopGroup.class;
            PREFERRED_SERVER_SOCKET_CHANNEL_CLASS = EpollServerSocketChannel.class;
            PREFERRED_SOCKET_CHANNEL_CLASS = EpollSocketChannel.class;
        } else if (KQueue.isAvailable()) {
            PREFERRED = "kqueue";
            PREFERRED_EVENT_LOOP_GROUP_CLASS = KQueueEventLoopGroup.class;
            PREFERRED_SERVER_SOCKET_CHANNEL_CLASS = KQueueServerSocketChannel.class;
            PREFERRED_SOCKET_CHANNEL_CLASS = KQueueSocketChannel.class;
        } else {
            PREFERRED = "nio";
            PREFERRED_EVENT_LOOP_GROUP_CLASS = NioEventLoopGroup.class;
            PREFERRED_SERVER_SOCKET_CHANNEL_CLASS = NioServerSocketChannel.class;
            PREFERRED_SOCKET_CHANNEL_CLASS = NioSocketChannel.class;
        }
    }

    public static EventLoopGroup createPreferredEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        try {
            return PREFERRED_EVENT_LOOP_GROUP_CLASS.getDeclaredConstructor(int.class, ThreadFactory.class).newInstance(nThreads, threadFactory);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
