FROM alpine:latest

COPY ./java-image/ /opt/ws2tcp

WORKDIR /opt/ws2tcp
CMD ["./bin/ws2tcp"]