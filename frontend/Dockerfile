FROM node:20
WORKDIR /app

ARG BACKEND_ENDPOINT
ENV BACKEND_ENDPOINT=$BACKEND_ENDPOINT

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build
RUN npm install -g serve

EXPOSE 3000

CMD ["serve", "-s", "dist", "--single", "-l", "3000"]
