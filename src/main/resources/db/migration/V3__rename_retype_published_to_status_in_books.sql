--status 列を追加
ALTER TABLE books
    ADD COLUMN status TEXT DEFAULT 'UNPUBLISHED';
--NOT NULL 制約を付ける
ALTER TABLE books
    ALTER COLUMN status SET NOT NULL;
--CHECK 制約で許可値を限定
ALTER TABLE books
    ADD CONSTRAINT books_status_check CHECK (status IN ('UNPUBLISHED', 'PUBLISHED'));
--旧カラムを削除
ALTER TABLE books
    DROP COLUMN published;