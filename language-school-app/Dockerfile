FROM node:22-alpine AS frontend-client-build
WORKDIR /app
COPY . .
CMD ls -a
RUN npm ci && npm run build
EXPOSE 5173
ENTRYPOINT ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
