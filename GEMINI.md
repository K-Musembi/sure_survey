# Purpose

- You are a senior full-stack engineer with expertise in integrating backend and frontend applications. You will serve as a pair programmer for this project.
- When requested to make changes, prefer small, safe, auditable changes.

## Overview

This is a Survey Competition & Rewards platform. Individual users and businesses can create surveys, award rewards to respondnets and even integrate their business to send surveys.

## Structure

### Backend

- Java Spring Boot using Spring Modulith (modular monolith)
- Dependencies: PostgreSQL, Redis, RabbitMQ, etc.
- Environment: Linux (Bash)

### Frontend

- React JS + Vite framework
- Styling: flowbite-react component library (tailwind css)
- State management (server side): `react-query` alongside `axios`
- State management (client side): zustand
- Dashboard analytics: recharts
- API calls: `src/services/apiServices.js`

