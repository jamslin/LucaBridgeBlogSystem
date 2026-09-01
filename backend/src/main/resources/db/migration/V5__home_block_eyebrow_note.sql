-- Two more trilingual fields on a home block, so copy that was only in the
-- frontend becomes editable content.
--
-- eyebrow — the small label above a block's headline ("與社區同行" over the hero,
--           "招募義工 · VOLUNTEER" over the support band). It was rendering the
--           company name on the hero, which is not what the comps show.
-- note    — the fine print beside the support band's button ("約 5 分鐘 · 11 個
--           欄位", "18 歲以下需家長簽署同意書"). This one matters: it is a factual
--           claim about the registration form's length, and with it hardcoded the
--           claim silently goes stale the moment the form changes.
--
-- Both nullable: every existing block keeps working, and blocks that have no use
-- for them (STAT, QUICK_LINK) simply leave them empty. Like title/subtitle, a
-- newline is meaningful — the note renders as two lines.

ALTER TABLE home_block_text ADD COLUMN IF NOT EXISTS tc_eyebrow varchar(120);
ALTER TABLE home_block_text ADD COLUMN IF NOT EXISTS en_eyebrow varchar(120);
ALTER TABLE home_block_text ADD COLUMN IF NOT EXISTS sc_eyebrow varchar(120);

ALTER TABLE home_block_text ADD COLUMN IF NOT EXISTS tc_note varchar(300);
ALTER TABLE home_block_text ADD COLUMN IF NOT EXISTS en_note varchar(300);
ALTER TABLE home_block_text ADD COLUMN IF NOT EXISTS sc_note varchar(300);
