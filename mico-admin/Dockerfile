# Stage 1
FROM node:8.11.2-alpine as node

WORKDIR /usr/src/app

COPY package*.json ./

# Run npm install and install all dependencies
# TODO no-package-lock necessary?
RUN npm install --no-package-lock

# Copy the rest of the project
COPY . .

# Subsequent builds are faster in case the package.json did not change.
RUN npm run build lint -- --force

# Stage 2
FROM nginx:1.13.12-alpine

COPY --from=node /usr/src/app/dist/mico-admin /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

# COPY nginx.conf /etc/nginx/nginx.conf
# WORKDIR /usr/share/nginx/html
# COPY dist/mico-admin/ .
