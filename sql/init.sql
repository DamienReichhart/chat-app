-- Drop the database if it exists.
-- (Note: To drop a database, you must be connected to a different database, such as "postgres".)
DROP DATABASE IF EXISTS ap4;
CREATE DATABASE ap4;

-- Connect to the ap4 database externally (e.g., in psql: \c ap4)


-- Drop existing tables if they exist.
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS chat_participants;
DROP TABLE IF EXISTS chats;
DROP TABLE IF EXISTS users;

-- Create the users table.
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users ADD CONSTRAINT users_unique_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT users_unique_email UNIQUE (email);

-- Create the chats table.
CREATE TABLE chats (
                       id SERIAL PRIMARY KEY,
                       chat_type VARCHAR(10) NOT NULL,
                       name VARCHAR(100),
                       description TEXT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE chats ADD CONSTRAINT chat_unique_name UNIQUE (name);


-- Create the chat_participants table.
CREATE TABLE chat_participants (
                                   id SERIAL PRIMARY KEY,
                                   joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   role VARCHAR(10) NOT NULL,
                                   user_id INT NOT NULL,
                                   chat_id INT NOT NULL,
                                   CONSTRAINT fk_user FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,
                                   CONSTRAINT fk_chat FOREIGN KEY (chat_id)
                                       REFERENCES chats(id)
                                       ON DELETE CASCADE
);

-- Create the messages table.
CREATE TABLE messages (
                          id SERIAL PRIMARY KEY,
                          content TEXT,
                          content_type VARCHAR(10) NOT NULL,
                          attachment_link TEXT,
                          file_data BYTEA,
                          file_name VARCHAR(255),
                          file_type VARCHAR(50),
                          timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          pinned BOOLEAN NOT NULL DEFAULT FALSE,
                          anonymous BOOLEAN NOT NULL DEFAULT FALSE,
                          chat_id INT NOT NULL,
                          sender_id INT NOT NULL,
                          CONSTRAINT fk_message_chat FOREIGN KEY (chat_id)
                              REFERENCES chats(id)
                              ON DELETE CASCADE,
                          CONSTRAINT fk_message_sender FOREIGN KEY (sender_id)
                              REFERENCES users(id)
                              ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION text_to_bool(val text)
    RETURNS boolean AS $$
BEGIN
    IF lower(val) = 'true' THEN
        RETURN true;
    ELSIF lower(val) = 'false' THEN
        RETURN false;
    ELSE
        RAISE EXCEPTION 'Valeur booléenne invalide : %', val;
    END IF;
END;
$$ LANGUAGE plpgsql IMMUTABLE STRICT;

CREATE CAST (text AS boolean)
    WITH FUNCTION text_to_bool(text)
    AS IMPLICIT;

CREATE CAST (varchar AS  boolean)
    WITH FUNCTION text_to_bool(text)
    AS IMPLICIT;