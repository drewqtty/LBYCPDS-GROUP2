package lbycpds.group2.model;

import java.util.List;

// One page of a lesson. The body is plain text: blank lines separate paragraphs, lines starting
// with "- " are bullets, lines like "1. " are numbered steps, lines starting with "## " are
// sub-headings, and text between ``` lines is shown as a code or diagram block.
public class LessonPage {
    private final String title;
    private final String body;
    private final List<PageBlock> blocks;

    public LessonPage(String title, String body) {
        this.title = title;
        this.body = body;
        this.blocks = PageBlock.parse(body);
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }

    // The body split into pieces that the lesson screen can show
    public List<PageBlock> getBlocks() { return blocks; }
}