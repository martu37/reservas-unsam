# Compila el front a archivos estáticos y los sirve con nginx
FROM node:24-alpine AS build
WORKDIR /app
RUN npm install -g pnpm@12.3.4
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
# La dirección del backend queda fija al compilar. En Railway se pasa como
# variable; si no se pasa, el front usa http://localhost:8080
ARG VITE_API_URL
RUN pnpm build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
