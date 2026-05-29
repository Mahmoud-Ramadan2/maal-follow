# Use a Node.js image that satisfies Vite 7's engine requirement
FROM node:20.19-alpine3.20 AS build-stage

# Set the working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install all dependencies needed for the build (including devDependencies)
RUN npm ci

# Copy the rest of the application code
COPY . .

# Build the application
RUN npm run build

# Use a lightweight Nginx image to serve the static files
FROM nginx:1.25.2-alpine

# Copy the built files from the build stage to the Nginx HTML directory
COPY --from=build-stage /app/dist /usr/share/nginx/html


# Expose port 80 for HTTP access
EXPOSE 80

# Start Nginx in the foreground
CMD ["nginx", "-g", "daemon off;"]
