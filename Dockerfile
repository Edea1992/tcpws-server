FROM alpine:latest

COPY ./ws2tcp/ /opt/ws2tcp

RUN chmod -R +x /opt/ws2tcp/bin && \
    for file in /opt/ws2tcp/bin/*; do ln -s $file /usr/sbin/$(basename $file); done

EXPOSE 80

ENTRYPOINT ["ws2tcp"]