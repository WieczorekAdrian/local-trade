ALTER TABLE users_entity
    ADD CONSTRAINT uc_usersentity_email UNIQUE (email);