FROM ubuntu:latest
LABEL authors="kruemelnerd"

ENTRYPOINT ["top", "-b"]