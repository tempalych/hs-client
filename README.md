# hs client
Client for HS demo project.
Server is [here](https://github.com/tempalych/hs-server).

## Usage
Collect deps
```
$ npm install
```

Local dev run
```
$ npm run dev
```

Build static resources
```
$ npm run release
```

Docker build image
```
docker build . /path/to/env -t hs-client
```

Docker run image
```
docker run --env-file -i -p 80:80 hs-client
```