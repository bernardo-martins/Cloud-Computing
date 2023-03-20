FROM ubuntu:20.04
WORKDIR .
COPY Testing/images Testing/images
COPY Testing/test-utils.js Testing
COPY Testing/workload1.yml Testing
COPY Testing/create-users.yml Testing
COPY Testing/create-channels.yml Testing
COPY Testing/create-messages.yml Testing

ENV DEBIAN_FRONTEND=noninteractive 

RUN apt-get update && \
      apt-get upgrade -y

RUN apt install sudo
RUN apt-get install curl -y
RUN apt install git -y

RUN curl -fsSL https://deb.nodesource.com/setup_14.x | sudo -E bash -
RUN apt-get install -y nodejs

RUN npm i faker
RUN npm install fs -y
RUN npm install path -y
RUN npm install -g artillery -y
RUN npm install -g faker -y
RUN npm install -g node-fetch -save -y
RUN npm install -g https://github.com/preguica/artillery-plugin-metrics-by-endpoint.git -y
RUN npm i http-server -g -y

CMD ["http-server"]
 

#RUN artillery run Testing/create-users.yml
#RUN artillery run Testing/create-channels.yml
#RUN artillery run Testing/create-messages.yml
#RUN artillery run Testing/workload1.yml