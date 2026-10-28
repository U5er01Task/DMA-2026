#!/usr/bin/env python3
"""Собирает evidence/report.md: текст задания из task_6.md, скриншоты и строки лога."""
import os
import re
import sys

root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
evidence = os.path.join(root, "evidence")
lines = open(os.path.join(root, "task_6.md"), encoding="utf-8").read().split("\n")

tasks = []  # (подпись, текст)
mode = None
topic = 0
for line in lines:
    m = re.match(r"## Тема (\d)", line)
    if m:
        topic, mode = int(m.group(1)), None
    elif line.startswith("### Задания для закрепления"):
        mode = "topic"
    elif line.startswith("## 50 Смеш"):
        mode, topic = "mixed", 0
    elif line.startswith("#") and not line.startswith("###"):
        mode = None
    elif mode and re.match(r"\d+\.\s", line):
        num = int(line.split(".")[0])
        label = f"Тема {topic}, задание {num}" if mode == "topic" else f"Задание {num} из 50"
        tasks.append([label, line])
    elif mode and tasks and line.strip() and (line.startswith(" ") or line.startswith("-")):
        tasks[-1][1] += "\n" + line

out = ["# Отчёт по практической работе: скриншоты заданий", ""]
missing = []
for n, (label, text) in enumerate(tasks[: int(sys.argv[2]) + 1 if len(sys.argv) > 2 else None]):
    start = int(sys.argv[1]) if len(sys.argv) > 1 else 0
    if n < start:
        continue
    k = f"{n + 1:02d}"
    out += [f"## {n + 1}. {label}", "", text, ""]
    for suffix, title in (("", "Экран приложения"), ("_shade", "Шторка уведомлений")):
        if os.path.exists(os.path.join(evidence, f"task_{k}{suffix}.png")):
            out += [f"**{title}:**", "", f"![task {k}](task_{k}{suffix}.png)", ""]
    log_path = os.path.join(evidence, f"task_{k}.log")
    log = open(log_path, encoding="utf-8", errors="replace").read().strip().split("\n") if os.path.exists(log_path) else []
    log = [x for x in log if x]
    if log:
        out += ["**Logcat:**", "", "```", *log[:12], "```", ""]
    else:
        missing.append(n + 1)
        out += ["> ⚠ В Logcat нет строк A80_: задание нужно снять вручную (см. CHECKLIST.md).", ""]
    out += ["---", ""]

open(os.path.join(evidence, "report.md"), "w", encoding="utf-8").write("\n".join(out))
print(f"report.md готов, заданий: {len(tasks)}")
if missing:
    print("Без лога (проверить вручную):", ", ".join(map(str, missing)))
