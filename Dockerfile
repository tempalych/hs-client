FROM clojure:openjdk-18-tools-deps-alpine AS builder
RUN apk add git npm

WORKDIR /usr/src/app
# copy the package.json to install dependencies
COPY package*.json ./

# Install the dependencies and make the folder
RUN npm install
COPY . .
RUN npm run release

FROM nginx:alpine
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /usr/src/app/public /usr/share/nginx/html

EXPOSE 80

ENTRYPOINT ["nginx", "-g", "daemon off;"]