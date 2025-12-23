CREATE TABLE IF NOT EXISTS phone_record (
  phone_key       TEXT PRIMARY KEY,

  name            TEXT NULL,
  price           NUMERIC NULL,
  currency        TEXT NULL,
  address         TEXT NULL,

  services_json   TEXT NULL,

  comment         TEXT NULL,
  visited         INTEGER NULL,  -- 0/1
  rating          INTEGER NULL,  -- 1..5
  finished        TEXT NULL,     -- YES/NO/PARTIALLY/HAND/ORA

  notes           TEXT NULL,
  tags_json       TEXT NULL,

  created_at      TEXT NOT NULL,
  updated_at      TEXT NOT NULL
);
