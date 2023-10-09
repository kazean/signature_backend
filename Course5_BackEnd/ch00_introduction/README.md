# 실행환경
## Mysql
docker-compose -f /Users/admin/study/signature/ws/docker-compose/mysql/docker-compose.yaml up -d
## rabbitMQ
docker-compose -f /Users/admin/study/signature/ws/docker-compose/rabbitmq/docker-compose.yaml up -d
- rabbitMQ Container console
> rabbitmq-plugins enable rabbitmq_management
## elk
docker-compose -f /Users/admin/study/signature/ws/docker-compose/elk-stack/docker-compose.yaml up -d
# TICK
docker-compose -f /Users/admin/study/signature/ws/docker-compose/tick/docker-compose.yaml up -d