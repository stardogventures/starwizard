package io.stardog.starwizard.services.markdown;

import org.junit.Test;

import static org.junit.Assert.*;

public class MarkdownServiceTest {
    private MarkdownService service = MarkdownService.withDefaults();

    @Test
    public void markdownToHtml() {
        assertEquals("<p>Test line 1 <strong>bold</strong><br />Test second line</p>\n<p>Test new paragraph</p>\n",
                service.markdownToHtml("Test line 1 **bold**\nTest second line\n\nTest new paragraph"));
        assertEquals("<p>Test list</p>\n<ul><li>Item 1</li><li>Item 2</li></ul>\n",
                service.markdownToHtml("Test list\n\n- Item 1\n- Item 2"));
        assertEquals("<p>Test numbered list</p>\n<ol><li>Item 1</li><li>Item 2</li></ol>\n",
                service.markdownToHtml("Test numbered list\n\n1. Item 1\n2. Item 2"));
        assertEquals("<p><a href=\"http://example.com\">Test Link</a></p>\n",
                service.markdownToHtml("[Test Link](http://example.com)"));
        assertEquals("<p><img src=\"https://example.com/src\" alt=\"My Image\" /></p>\n",
                service.markdownToHtml("![My Image](https://example.com/src)"));
    }

    @Test
    public void isSafeMarkdown() {
        assertTrue(service.isSafeMarkdown("Images and links are ok: [http://example.com] - ![My Image](https://example.com/src)"));
        assertTrue(service.isSafeMarkdown("This is a test\n<p>Test</p>"));
        assertTrue(service.isSafeMarkdown("Lists should be okay too:\n\n- Item 1\n- Item 2"));
        assertFalse(service.isSafeMarkdown("No scripts allowed \n<script>alert('hello')</script>"));
        assertFalse(service.isSafeMarkdown("No bad attributes allowed either \n<img onload='dosomethingbad()'>Test</p>"));
    }
}