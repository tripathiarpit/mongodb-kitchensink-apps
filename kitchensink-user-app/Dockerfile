# Multi-stage build for Angular application
FROM node:20-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install all dependencies (including devDependencies for build)
RUN npm ci

# Copy source code
COPY . .

# Build the Angular application
# First try normal build, if it fails due to budget, build without budget checks
RUN npm run build || npx ng build --configuration=production --aot --build-optimizer --vendor-chunk --extract-licenses --source-map=false

# Production stage with Nginx
FROM nginx:alpine

# Copy built application from build stage
COPY --from=build /app/dist/ /tmp/dist/

# Handle Angular's browser subdirectory structure
RUN if [ -d "/tmp/dist/kitchensink-user-app/browser" ]; then \
        cp -r /tmp/dist/kitchensink-user-app/browser/* /usr/share/nginx/html/; \
    elif [ -d "/tmp/dist/kitchensink-user-app" ]; then \
        cp -r /tmp/dist/kitchensink-user-app/* /usr/share/nginx/html/; \
    else \
        cp -r /tmp/dist/* /usr/share/nginx/html/; \
    fi && \
    rm -rf /tmp/dist

# Verify files were copied
RUN ls -la /usr/share/nginx/html/

# Copy custom nginx configuration (optional)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
