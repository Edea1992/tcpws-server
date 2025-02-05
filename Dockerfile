FROM alpine:edge

COPY ./build/image/ /opt/

WORKDIR /opt/bin
CMD ["./tcpws-server"]
