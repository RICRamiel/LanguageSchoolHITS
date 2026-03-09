FROM node:22-alpine AS frontend-client-build
WORKDIR /app
COPY language-school-app/package.json language-school-app/package-lock.json ./
RUN npm install --ci
COPY language-school-app/. .
RUN npm run build -- --configuration=production --output-path=./dist/browser && npm install -g @angular/cli
EXPOSE 4200
ENTRYPOINT ["ng", "serve", "--host", "0.0.0.0"]
