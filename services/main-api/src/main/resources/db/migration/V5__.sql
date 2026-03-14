ALTER TABLE trade_entity
    ALTER COLUMN buyer_id SET NOT NULL;

ALTER TABLE users_entity
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE users_entity
    ALTER COLUMN created_by SET NOT NULL;

ALTER TABLE users_entity
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE chat_message_entity
    ALTER COLUMN is_read SET NOT NULL;

ALTER TABLE trade_entity
    ALTER COLUMN seller_id SET NOT NULL;