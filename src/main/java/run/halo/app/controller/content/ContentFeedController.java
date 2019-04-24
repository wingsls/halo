package run.halo.app.controller.content;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;

import java.io.IOException;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author : RYAN0UP
 * @date : 2019-03-21
 */
@Controller
public class ContentFeedController {

    private final PostService postService;

    private final OptionService optionService;

    private final FreeMarkerConfigurer freeMarker;

    private final static String UTF_8_SUFFIX = ";charset=UTF-8";

    private final static String XML_MEDIA_TYPE = MediaType.APPLICATION_XML_VALUE + UTF_8_SUFFIX;

    public ContentFeedController(PostService postService,
                                 OptionService optionService,
                                 FreeMarkerConfigurer freeMarker) {
        this.postService = postService;
        this.optionService = optionService;
        this.freeMarker = freeMarker;
    }

    /**
     * Get post rss
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"feed", "feed.xml", "rss", "rss.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String feed(Model model) throws IOException, TemplateException {
        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/rss.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get atom.xml
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"atom", "atom.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String atom(Model model) throws IOException, TemplateException {
        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getPostPageSize())));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/atom.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get sitemap.xml.
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = {"sitemap", "sitemap.xml"}, produces = XML_MEDIA_TYPE)
    @ResponseBody
    public String sitemapXml(Model model) throws IOException, TemplateException {
        model.addAttribute("posts", buildPosts(null));
        Template template = freeMarker.getConfiguration().getTemplate("common/web/sitemap_xml.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Get sitemap.html.
     *
     * @param model model
     * @return String
     */
    @GetMapping(value = "sitemap.html", produces = MediaType.TEXT_PLAIN_VALUE)
    public String sitemapHtml(Model model) {
        model.addAttribute("posts", buildPosts(null));
        return "common/web/sitemap_html";
    }

    /**
     * Get robots.
     *
     * @param model model
     * @return String
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    @GetMapping(value = "robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots(Model model) throws IOException, TemplateException {
        Template template = freeMarker.getConfiguration().getTemplate("common/web/robots.ftl");
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }

    /**
     * Builds page info for post.
     *
     * @param size page size
     * @return page info
     */
    @NonNull
    private Pageable buildPostPageable(int size) {
        return PageRequest.of(0, size, Sort.by(DESC, "createTime"));
    }

    /**
     * Build posts for feed
     *
     * @param pageable pageable
     * @return List<Post>
     */
    private List<Post> buildPosts(Pageable pageable) {
        if (pageable == null) {
            return postService.listAllBy(PostStatus.PUBLISHED);
        }

        return postService.pageBy(PostStatus.PUBLISHED, pageable).map(postService::filterIfEncrypt).getContent();
    }
}