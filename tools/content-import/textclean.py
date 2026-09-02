"""
Whitespace-and-corruption-only cleaner for the legacy LucaBridge event copy.

HARD RULE (Jams, 2026-09-01): never alter a Chinese character or word.
This module may ONLY:
  - delete corrupt code points (U+FFFD, private-use area)
  - change whitespace (remove, collapse, or insert line breaks)

Every other code point must survive, in the same order. `signature()` and
`assert_invariant()` make that mechanically checkable rather than a matter of care:
the caller is expected to abort the whole run if a single character differs.
"""
import re
import unicodedata

# --- what counts as "corrupt" -------------------------------------------------
# U+FFFD is baked into the source .docx: non-BMP emoji were destroyed (as surrogate
# pairs) before the file was ever saved. Unrecoverable from these files.
# Private-use area code points are Wingdings/Symbol font artifacts (e.g. U+F03A9).
def is_corrupt(ch: str) -> bool:
    o = ord(ch)
    return (
        ch == "�"
        or 0xE000 <= o <= 0xF8FF          # BMP private use
        or 0xF0000 <= o <= 0xFFFFD        # supplementary private use A
        or 0x100000 <= o <= 0x10FFFD      # supplementary private use B
    )

# Every space-ish code point Word/Facebook leaves behind.
WS = set(" \t\r\n 　​‌‍﻿    ")

def is_ws(ch: str) -> bool:
    return ch in WS


def signature(s: str) -> str:
    """The part of the text that MUST NOT change: everything but whitespace and corruption."""
    return "".join(ch for ch in s if not is_ws(ch) and not is_corrupt(ch))


def assert_invariant(before: str, after: str, where: str = "") -> None:
    a, b = signature(before), signature(after)
    if a != b:
        # find first divergence so the failure is actionable, not just "differs"
        i = next((i for i in range(min(len(a), len(b))) if a[i] != b[i]), min(len(a), len(b)))
        raise AssertionError(
            f"CHARACTER INVARIANT VIOLATED at {where!r} (offset {i}): "
            f"before={a[max(0,i-15):i+15]!r} after={b[max(0,i-15):i+15]!r}"
        )


# --- the cleaning itself ------------------------------------------------------
def _is_cjk(ch: str) -> bool:
    o = ord(ch)
    return (
        0x3400 <= o <= 0x4DBF or 0x4E00 <= o <= 0x9FFF or 0xF900 <= o <= 0xFAFF
        or 0x20000 <= o <= 0x2FA1F
        or ch in "，。！？；：、「」『』（）【】《》…—～·"
    )

def _is_word(ch: str) -> bool:
    """CJK, or an ASCII alphanumeric — the two sides of a line-wrap artifact."""
    return _is_cjk(ch) or ch.isalnum()


def clean_inline(text: str) -> str:
    """Strip corruption, then remove line-wrap spaces. Whitespace + corruption only."""
    # 1. drop corrupt code points entirely
    s = "".join("" if is_corrupt(ch) else ch for ch in text)
    # 2. normalise every whitespace variant to a plain space
    s = "".join(" " if is_ws(ch) else ch for ch in s)
    # 3. collapse runs
    s = re.sub(r" {2,}", " ", s)
    # 4. remove a single space sitting between two word characters.
    #    Evidence: space gaps in the source cluster at a fixed ~42-char column,
    #    i.e. they are wrap artifacts, not authored spacing. Applied repeatedly
    #    because matches overlap ("心 一 直").
    prev = None
    while prev != s:
        prev = s
        s = re.sub(r"(?<=[^\x00-\x7F\w]) (?=[^\x00-\x7F\w])", "", s)
        s = "".join(s)
        out = []
        i = 0
        while i < len(s):
            if (s[i] == " " and 0 < i < len(s) - 1
                    and _is_word(s[i - 1]) and _is_word(s[i + 1])
                    and not (s[i - 1].isascii() and s[i - 1].isalnum()
                             and s[i + 1].isascii() and s[i + 1].isalnum())):
                i += 1          # drop this space
                continue
            out.append(s[i])
            i += 1
        s = "".join(out)
    return s.strip()


SENT_END = "。！？"

def reflow(text: str, target: int = 150) -> str:
    """
    Re-insert paragraph breaks. Nothing survived in the source (see above), so this is
    a presentation choice: group whole sentences into paragraphs of roughly `target`
    characters, breaking only after 。！？. Inserts newlines only — never edits a character.
    """
    s = clean_inline(text)
    if not s:
        return s
    # Never break inside a quotation — a sentence-final mark inside 「」 or 『』 is
    # part of the quoted speech, not the end of the narrator's sentence.
    OPEN, CLOSE = "「『\u201c", "」』\u201d"
    sentences, buf, depth = [], "", 0
    for ch in s:
        buf += ch
        if ch in OPEN:
            depth += 1
        elif ch in CLOSE:
            depth = max(0, depth - 1)
            if depth == 0 and len(buf) > 1 and buf[-2] in SENT_END:
                sentences.append(buf); buf = ""
        elif ch in SENT_END and depth == 0:
            sentences.append(buf)
            buf = ""
    # a closing quote directly after sentence-final punctuation ends the sentence too
    sentences = [x for x in sentences if x]
    if buf.strip():
        sentences.append(buf)

    paras, cur = [], ""
    for sent in sentences:
        cur += sent
        if len(cur) >= target:
            paras.append(cur.strip())
            cur = ""
    if cur.strip():
        if paras and len(cur) < 40:
            paras[-1] = (paras[-1] + cur).strip()   # don't leave a runt paragraph
        else:
            paras.append(cur.strip())
    return "\n\n".join(p for p in paras if p)
