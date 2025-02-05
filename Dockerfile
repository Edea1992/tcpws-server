FROM debian:latest

COPY ./build/image/ /opt/

WORKDIR /opt/bin
CMD ["./tcpws-server"]
