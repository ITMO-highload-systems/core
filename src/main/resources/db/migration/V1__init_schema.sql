

CREATE TABLE note
(
    note_id     SERIAL PRIMARY KEY,
    owner       TEXT,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE paragraph
(
    id        SERIAL PRIMARY KEY,
    note_id             INT REFERENCES note (note_id),
    title               VARCHAR(255),
    next_paragraph_id     INT REFERENCES paragraph(id),
    text                TEXT NOT NULL,
    last_update_user_id TEXT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paragraph_type      varchar(255)
);

CREATE TABLE execution_code_result (
    paragraph_id INT PRIMARY KEY REFERENCES paragraph (id),
    execution_result text
);

CREATE TABLE image_record
(
    id           SERIAL PRIMARY KEY,
    image_hash   VARCHAR(255) NOT NULL,
    paragraph_id INT REFERENCES paragraph (id) ON DELETE CASCADE
);

CREATE TABLE team
(
    team_id SERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL UNIQUE,
    owner TEXT
);

CREATE TABLE note_user_permission
(
    permission_id SERIAL PRIMARY KEY,
    note_id INT REFERENCES note (note_id) on delete cascade,
    user_id TEXT,
    permission    VARCHAR(50) NOT NULL,
    UNIQUE (user_id, note_id)
);

CREATE TABLE note_team_permission
(
    permission_id SERIAL PRIMARY KEY,
    team_id INT REFERENCES team (team_id) ON DELETE CASCADE,
    note_id INT REFERENCES note (note_id) ON DELETE CASCADE,
    permission VARCHAR(50) NOT NULL,
    UNIQUE (note_id, team_id)
);

CREATE TABLE team_user
(
    id SERIAL PRIMARY KEY,
    user_id TEXT,
    team_id INT REFERENCES team (team_id) ON DELETE CASCADE,
    UNIQUE (user_id, team_id)
)
