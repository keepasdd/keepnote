ALTER TABLE note
    ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否进入回收站：0=正常，1=回收站',
    ADD COLUMN deleted_time DATETIME NULL COMMENT '移入回收站时间';

CREATE INDEX idx_note_user_deleted ON note (user_id, is_deleted, deleted_time);
