FROM alpine:latest

COPY ./java-image/ /usr/

WORKDIR /usr/bin
CMD ["./tcpws-server"]