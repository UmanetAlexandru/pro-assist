-- 1) Rename name -> description (preserves data)
ALTER TABLE phone_record RENAME COLUMN name TO description;

-- 2) Add source_url (new, null for existing rows)
ALTER TABLE phone_record ADD COLUMN source_url TEXT NULL;
