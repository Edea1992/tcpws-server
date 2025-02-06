FROM alpine:latest

COPY ./ws2tcp/ /opt/ws2tcp

RUN chmod -R +x /opt/ws2tcp/bin && \
    ln -s /opt/ws2tcp/bin/ws2tcp /usr/sbin/ws2tcp

EXPOSE 80

ENTRYPOINT ["ws2tcp"]