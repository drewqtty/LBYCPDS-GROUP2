package lbycpds.group2.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// One piece of a lesson page, ready to be shown on screen.
// See LessonPage for the text format that PageBlock.parse reads.
public class PageBlock {

    public enum Type { HEADING, PARAGRAPH, BULLET, NUMBERED, CODE }

    private static final Pattern NUMBERED_LINE = Pattern.compile("^(\\d+\\.)\\s+(.*)$");

    private final Type type;
    private final String text;
    private final String label;   // "1." for a numbered step, the language for a code block, otherwise ""

    public PageBlock(Type type, String text, String label) {
        this.type = type;
        this.text = text;
        this.label = label;
    }

    public Type getType() { return type; }
    public String getText() { return text; }
    public String getLabel() { return label; }

    // Splits the text of a page into blocks:
    //   ## text          a sub-heading
    //   - text           a bullet
    //   1. text          a numbered step
    //   ```java ... ```  a code or diagram block (indentation is kept)
    //   anything else    paragraph text; lines up to the next blank line are joined into one paragraph
    public static List<PageBlock> parse(String body) {
        List<PageBlock> blocks = new ArrayList<>();
        if (body == null) {
            return blocks;
        }

        StringBuilder paragraph = new StringBuilder();
        String[] lines = body.split("\r?\n", -1);
        int i = 0;
        while (i < lines.length) {
            String trimmed = lines[i].trim();
            Matcher numbered = NUMBERED_LINE.matcher(trimmed);

            if (trimmed.startsWith("```")) {
                flush(blocks, paragraph);
                String language = trimmed.substring(3).trim();
                StringBuilder code = new StringBuilder();
                i++;
                while (i < lines.length && !lines[i].trim().startsWith("```")) {
                    code.append(lines[i]).append('\n');
                    i++;
                }
                String text = trimBlankLines(code.toString());
                if (!text.isEmpty()) {
                    blocks.add(new PageBlock(Type.CODE, text, language));
                }
            } else if (trimmed.isEmpty()) {
                flush(blocks, paragraph);
            } else if (trimmed.startsWith("## ")) {
                flush(blocks, paragraph);
                blocks.add(new PageBlock(Type.HEADING, trimmed.substring(3).trim(), ""));
            } else if (trimmed.startsWith("- ")) {
                flush(blocks, paragraph);
                blocks.add(new PageBlock(Type.BULLET, trimmed.substring(2).trim(), ""));
            } else if (numbered.matches()) {
                flush(blocks, paragraph);
                blocks.add(new PageBlock(Type.NUMBERED, numbered.group(2).trim(), numbered.group(1)));
            } else {
                if (paragraph.length() > 0) {
                    paragraph.append(' ');
                }
                paragraph.append(trimmed);
            }
            i++;
        }
        flush(blocks, paragraph);
        return blocks;
    }

    private static void flush(List<PageBlock> blocks, StringBuilder paragraph) {
        if (paragraph.length() > 0) {
            blocks.add(new PageBlock(Type.PARAGRAPH, paragraph.toString(), ""));
            paragraph.setLength(0);
        }
    }

    // Removes blank lines at the start and end of a code block but keeps the indentation
    private static String trimBlankLines(String code) {
        String[] lines = code.split("\n", -1);
        int start = 0;
        int end = lines.length;
        while (start < end && lines[start].isBlank()) {
            start++;
        }
        while (end > start && lines[end - 1].isBlank()) {
            end--;
        }
        return String.join("\n", Arrays.copyOfRange(lines, start, end));
    }
}