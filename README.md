
## What for reusing

- pom.xml
- application-dev.yaml
- application-test.yaml
- application-staging.yaml
- application-prod.yaml


## How to connect to data layer

Depending of the environment: development, stating or production the connection string could be:
- Development (docker container name): url: jdbc:postgresql://absencesdb:5432/${DB_NAME:absences_dev}
- Staging or Production (K8s Service DNS name): url: jdbc:postgresql://postgres-service:5432/${DB_NAME:${DB_NAME_DEVELOPMENT:absencesdev}}

## Set Rate Limiting on Endpoints:

This could be part of the yaml setup files:
```aiignore
endpoints:
  /api/auth/login:
    capacity: 50
    refill-rate: 5
    refill-duration: PT1M
  /api/users:
    capacity: 200
    refill-rate: 20
    refill-duration: PT30S
  /api/tasks:
    capacity: 300
    refill-rate: 30
    refill-duration: PT30S
```

