FROM alpine:latest

COPY ./build/image/ /opt/

WORKDIR /opt/bin
CMD ["./tcpws-server"]