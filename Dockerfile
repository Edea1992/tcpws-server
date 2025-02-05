FROM alpine:latest

COPY ./build/image/ /usr/local/sbin

WORKDIR /usr/local/sbin
CMD ["./tcpws-server"]
