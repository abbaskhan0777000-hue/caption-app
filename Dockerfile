FROM node:20-alpine AS base

# Install FFmpeg and libass in the container
RUN apk add --no-cache ffmpeg

WORKDIR /app

# Install dependencies
COPY package.json package-lock.json* ./
RUN npm ci || npm install

# Copy source files
COPY . .

# Build Next.js app
RUN npm run build

# Production image
FROM node:20-alpine AS runner
WORKDIR /app

ENV NODE_ENV=production
RUN apk add --no-cache ffmpeg

COPY --from=base /app/package.json ./
COPY --from=base /app/node_modules ./node_modules
COPY --from=base /app/.next ./.next
COPY --from=base /app/public ./public 2>/dev/null || true

EXPOSE 3000

CMD ["npm", "start"]
