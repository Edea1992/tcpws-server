FROM alpine:latest

COPY ./ws2tcp /opt

WORKDIR /opt/ws2tcp
CMD ["./bin/ws2tcp"]