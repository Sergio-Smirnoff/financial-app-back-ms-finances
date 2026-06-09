ALTER TABLE finances.outbox_event ADD COLUMN event_id    VARCHAR(64);
ALTER TABLE finances.outbox_event ADD COLUMN ce_type    VARCHAR(120);
ALTER TABLE finances.outbox_event ADD COLUMN ce_source  VARCHAR(255);
ALTER TABLE finances.outbox_event ADD COLUMN data_schema VARCHAR(512);
ALTER TABLE finances.outbox_event RENAME COLUMN payload TO data_json;
ALTER TABLE finances.outbox_event ALTER COLUMN topic TYPE VARCHAR(249);
ALTER TABLE finances.outbox_event ALTER COLUMN aggregate_key TYPE VARCHAR(64);

UPDATE finances.outbox_event
   SET event_id     = 'migrated-' || id::text,
       ce_type      = 'finances.transaction.created',
       ce_source    = 'ms-finances',
       data_schema  = 'https://schemas.financial-app/finances/transaction-created/v1';

ALTER TABLE finances.outbox_event ALTER COLUMN event_id SET NOT NULL;
ALTER TABLE finances.outbox_event ADD CONSTRAINT uq_finances_outbox_event_id UNIQUE (event_id);
