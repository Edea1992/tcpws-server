FROM alpine:latest

COPY ./ws2tcp/ /opt/ws2tcp

WORKDIR /opt/ws2tcp
RUN chmod -R +x ./bin

EXPOSE 80

ENTRYPOINT ["./bin/ws2tcp"]