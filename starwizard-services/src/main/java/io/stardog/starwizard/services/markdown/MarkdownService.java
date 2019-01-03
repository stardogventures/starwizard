package io.stardog.starwizard.services.markdown;

import org.apache.commons.text.StringEscapeUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * MarkdownService converts Markdown input to HTML, screening out common XSS vulnerabilities using OWASP's Java
 * HTML Sanitizer.
 *
 * The default settings used by MarkdownService.withDefaults() are settings that allow links, images, tables,
 * and common markup, but disallow most other elements.
 */
@Singleton
public class MarkdownService {
    private final static PolicyFactory DEFAULT_SANITIZE_POLICY = new HtmlPolicyBuilder()
            .allowCommonBlockElements()
            .allowStandardUrlProtocols()
            .allowCommonInlineFormattingElements()
            .allowElements("a", "pre", "img", "table", "tbody", "thead", "tfoot", "th", "tr", "td")
            .allowAttributes("start").onElements("ol")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src", "alt", "title").onElements("img")
            .toFactory();
    private final static Parser DEFAULT_MARKDOWN_PARSER = Parser.builder().build();
    private final static HtmlRenderer DEFAULT_HTML_RENDERER = HtmlRenderer.builder().softbreak("<br />").build();

    private final PolicyFactory sanitizePolicy;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public static MarkdownService withDefaults() {
        return new MarkdownService(DEFAULT_SANITIZE_POLICY, DEFAULT_MARKDOWN_PARSER, DEFAULT_HTML_RENDERER);
    }

    public static MarkdownService of(PolicyFactory sanitizePolicy, Parser markdownParser, HtmlRenderer htmlRenderer) {
        return new MarkdownService(sanitizePolicy, markdownParser, htmlRenderer);
    }

    @Inject
    private MarkdownService(PolicyFactory sanitizePolicy, Parser markdownParser, HtmlRenderer htmlRenderer) {
        this.sanitizePolicy = sanitizePolicy;
        this.markdownParser = markdownParser;
        this.htmlRenderer = htmlRenderer;
    }

    /**
     * Converts Markdown to HTML by parsing it, turning it into HTML, and running the resulting HTML through the
     * html-sanitization policy.
     *
     * @param markdown  string containing Markdown
     * @return  string containing HTML output
     */
    public String markdownToHtml(String markdown) {
        Node node = markdownParser.parse(markdown);
        String html = htmlRenderer.render(node);
        return sanitizePolicy.sanitize(html);
    }

    /**
     * Tests whether a given piece of Markdown is safe, by comparing it to the results of sanitization.
     * @param markdown  untrusted raw Markdown
     * @return  true if the Markdown is valid input
     */
    public boolean isSafeMarkdown(String markdown) {
        Node node = markdownParser.parse(markdown);
        String html = htmlRenderer.render(node);
        html = html.replaceAll(">\n<", "><");
        html = StringEscapeUtils.unescapeHtml4(html).trim();

        String safeHtml = sanitizePolicy.sanitize(html);
        safeHtml = StringEscapeUtils.unescapeHtml4(safeHtml).trim();

        return safeHtml.equals(html);
    }
}
