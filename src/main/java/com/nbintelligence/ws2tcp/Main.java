package com.nbintelligence.ws2tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.*;

import java.net.InetAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    final static ThreadFactory VIRTUAL_THREAD_FACTORY = Thread.ofVirtual().factory();

    public static void main(String[] args) {
        var masterGroup = new NioEventLoopGroup(1, VIRTUAL_THREAD_FACTORY);
        var workerGroup = new NioEventLoopGroup(1024, VIRTUAL_THREAD_FACTORY);
        try {
            new ServerBootstrap()
                .group(masterGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel tunnelServerChannel) {
                        Runtime.getRuntime().addShutdownHook(new Thread(
                            tunnelServerChannel.close()::syncUninterruptibly
                        ));
                    }
                })
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel tunnelServerChannel) {
                        new Bootstrap()
                            .group(workerGroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(NioSocketChannel tcpClientChannel) {
                                    var currentCloseFrame = new AtomicReference<CloseWebSocketFrame>();

                                    tcpClientChannel.config()
                                        .setAllowHalfClosure(true);

                                    tcpClientChannel.pipeline()
                                        .addLast(new SimpleChannelInboundHandler<>(ByteBuf.class) {
                                            @Override
                                            public void channelInactive(ChannelHandlerContext ctx) {
                                                tunnelServerChannel.close();
                                            }

                                            @Override
                                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                                if (evt instanceof ChannelInputShutdownEvent) {
                                                    if (currentCloseFrame.compareAndSet(null, new CloseWebSocketFrame(WebSocketCloseStatus.NORMAL_CLOSURE))) {
                                                        tunnelServerChannel.writeAndFlush(currentCloseFrame.get()).syncUninterruptibly();
                                                    } else {
                                                        tunnelServerChannel.writeAndFlush(currentCloseFrame.get()).syncUninterruptibly();
                                                        tcpClientChannel.close();
                                                    }
                                                }
                                            }

                                            @Override
                                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf bytes) {
                                                tunnelServerChannel.writeAndFlush(new BinaryWebSocketFrame(bytes)).syncUninterruptibly();
                                            }
                                        });

                                    tunnelServerChannel.pipeline()
                                        .addLast(new WebSocketServerProtocolHandler(
                                            WebSocketServerProtocolConfig.newBuilder()
                                                .websocketPath("/tunnel")
                                                .handleCloseFrames(false)
                                                .build()
                                        ))
                                        .addLast(new SimpleUserEventChannelHandler<>(WebSocketServerProtocolHandler.HandshakeComplete.class) {
                                            @Override
                                            protected void eventReceived(ChannelHandlerContext ctx, WebSocketServerProtocolHandler.HandshakeComplete evt) {
                                                ctx.pipeline().replace(this, ctx.name(), new SimpleChannelInboundHandler<>(WebSocketFrame.class) {
                                                    @Override
                                                    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
                                                        try {
                                                            switch (frame) {
                                                                case BinaryWebSocketFrame binaryFrame ->
                                                                    tcpClientChannel.writeAndFlush(binaryFrame.content().retain()).syncUninterruptibly();

                                                                case CloseWebSocketFrame closeFrame -> {
                                                                    if (currentCloseFrame.compareAndSet(null, closeFrame.retain())) {
                                                                        tcpClientChannel.shutdownOutput().syncUninterruptibly();
                                                                    } else {
                                                                        tcpClientChannel.close().syncUninterruptibly();
                                                                    }
                                                                }

                                                                default -> throw new CorruptedWebSocketFrameException();
                                                            }
                                                        } finally {
                                                            frame.release();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                }
                            }).connect(InetAddress.getLoopbackAddress(), 1080).syncUninterruptibly();
                    }
                }).bind(80).addListener(future -> {
                    if (future.isSuccess()) {
                        System.out.println("Server started on port 80");
                    }
                }).channel().closeFuture().syncUninterruptibly();
        } finally {
            System.out.println("Server stopped");
            masterGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
