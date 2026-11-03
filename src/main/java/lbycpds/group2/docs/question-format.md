# Question Bank Format

Question banks are plain text files in `src/main/resources/questions/`.
Use `circuits.txt` for Fundamentals of Electrical Circuits and `dsa.txt` for Data Structures and Algorithms.

## Line types

| Type | Line layout |
|---|---|
| Course | `COURSE#course name` |
| Topic | `TOPIC#topic#next topic 1#next topic 2#...` |
| Multiple choice | `MC#id#topic#difficulty#question#A#B#C#D#answerLetter` |
| Identification | `ID#id#topic#difficulty#question#answer` |
| Problem solving | `PS#id#topic#difficulty#question#modelAnswer#keyword1#keyword2#...` |

Fields are separated by `#`. One question per line.

## Rules

1. The first line of every file is the `COURSE` line.
2. A `TOPIC` line lists a topic and the topics it unlocks. A topic that no other topic points to is unlocked from the start.
3. Never use `#` inside a question, choice, or answer.
4. `id` is a whole number, unique within its file. Never renumber or reuse an id, because saved student history points to it.
5. Spell every topic exactly as it appears in the `TOPIC` lines, including capital letters.
6. Difficulty is 1, 2, or 3 only.
7. Every topic has exactly 3 questions, one at each level.
8. Suggested types: level 1 is `MC`, level 2 is `ID`, level 3 is `PS`.
9. Multiple choice has exactly four choices, and the answer is the letter A, B, C, or D.
10. Identification answers are checked by exact match (capital letters and extra spaces are ignored), so ask for one short, clear answer.
11. Problem solving checks that every keyword appears in the student's answer. Use short, distinctive keywords, and plain numbers without units (`27`, not `27 W`).
12. Blank lines and lines starting with `//` are skipped.

## Example

COURSE#Fundamentals of Electrical Circuits
TOPIC#Ohm's Law#Series Circuits
TOPIC#Series Circuits
MC#1#Ohm's Law#1#What is the unit of electrical resistance?#Volt#Ampere#Ohm#Watt#C
ID#2#Ohm's Law#2#Which law relates voltage, current, and resistance?#Ohm's Law
PS#3#Ohm's Law#3#A 9 V source is across a 3 ohm resistor. Find the current and the power.#I = 3 A and P = 27 W#3#27