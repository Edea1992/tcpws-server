FROM alpine:latest

COPY ./java-image/ /usr/local/sbin

WORKDIR /usr/local/sbin
CMD ["./tcpws-server"]
