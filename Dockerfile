FROM alpine:latest

COPY ./ws2tcp/ /opt/ws2tcp

WORKDIR /opt/ws2tcp
CMD ["./bin/ws2tcp"]