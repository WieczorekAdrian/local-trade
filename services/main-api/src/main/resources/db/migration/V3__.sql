ALTER TABLE chat_message_entity
    ADD advertisement_id INTEGER;

ALTER TABLE chat_message_entity
    ADD CONSTRAINT FK_CHATMESSAGEENTITY_ON_ADVERTISEMENT FOREIGN KEY (advertisement_id) REFERENCES advertisement_entity (id);

ALTER TABLE chat_message_entity
    ADD COLUMN is_read BOOLEAN DEFAULT FALSE;