curl 'localhost:8083/connectors/' -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
-d '{
   "name":"xsender-connector",
   "config":{
      "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
      "tasks.max": "1",
      "database.hostname": "xsender-db",
      "database.port": "5432",
      "database.user": "xsender_username",
      "database.password": "xsender_password",
      "database.dbname": "xsender_db",
      "database.server.name": "dbserver1",
      "schema.include.list": "public",
      "table.include.list": "public.outboxevent",
      "tombstones.on.delete": "false",
      "transforms": "outbox",
      "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
      "transforms.outbox.table.fields.additional.placement": "type:header:eventType",
      "key.converter": "org.apache.kafka.connect.json.JsonConverter",
      "key.converter.schemas.enable": "false",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false"
   }
}'
