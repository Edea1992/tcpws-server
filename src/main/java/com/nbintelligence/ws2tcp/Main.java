package com.nbintelligence.ws2tcp;

import com.nbintelligence.ws2tcp.internal.Platform;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.*;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) {
        var masterGroup = Platform.createPreferredEventLoopGroup(1, Thread.ofVirtual().factory());
        var workerGroup = Platform.createPreferredEventLoopGroup(1024, Thread.ofVirtual().factory());
        try {
            new ServerBootstrap()
                .group(masterGroup, workerGroup)
                .channel(Platform.PREFERRED_SERVER_SOCKET_CHANNEL_CLASS)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel tunnelServerChannel) {
                        new Bootstrap()
                            .group(workerGroup)
                            .channel(Platform.PREFERRED_SOCKET_CHANNEL_CLASS)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel tcpClientChannel) {
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
                        System.out.println("Server started on port 80. [" + Platform.PREFERRED + "]");
                    }
                }).channel().closeFuture().syncUninterruptibly();
        } finally {
            System.out.println("Server stopped");
            masterGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
