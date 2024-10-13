CREATE TABLE "user"
(
    user_id  SERIAL PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE note
(
    note_id     SERIAL PRIMARY KEY,
    owner       INT REFERENCES "user" (user_id),
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
    last_update_user_id INT REFERENCES "user" (user_id),
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paragraph_type      varchar(255)
);

CREATE TABLE image_record
(
    id           SERIAL PRIMARY KEY,
    image_hash   VARCHAR(255) NOT NULL,
    paragraph_id INT REFERENCES paragraph (id)
);

CREATE TABLE team
(
    team_id SERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL UNIQUE,
    owner   INT REFERENCES "user" (user_id)
);

CREATE TABLE note_user_permission
(
    permission_id SERIAL PRIMARY KEY,
    note_id INT REFERENCES note (note_id) on delete cascade,
    user_id INT REFERENCES "user" (user_id) on delete cascade,
    permission    VARCHAR(50) NOT NULL,
    UNIQUE (user_id, note_id)
);

CREATE TABLE note_team_permissions
(
    permission_id SERIAL PRIMARY KEY,
    team_id       INT REFERENCES team (team_id),
    note_id       INT REFERENCES note (note_id),
    permission    VARCHAR(50) NOT NULL
);

CREATE TABLE user_team
(
    id SERIAL PRIMARY KEY,
    user_id       INT REFERENCES "user" (user_id),
    team_id       INT REFERENCES team (team_id),
    UNIQUE (user_id, team_id)
)
