module com.nbintelligence.ws2tcp {
    exports com.nbintelligence.ws2tcp;

    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires io.netty.transport;
    requires io.netty.transport.classes.epoll;
    requires io.netty.transport.classes.kqueue;
}