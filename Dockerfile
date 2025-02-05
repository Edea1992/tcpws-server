FROM alpine:latest

COPY ./ws2tcp/ /opt/ws2tcp

WORKDIR /opt/ws2tcp
RUN chmod -R +x ./bin

CMD ["./bin/ws2tcp"]