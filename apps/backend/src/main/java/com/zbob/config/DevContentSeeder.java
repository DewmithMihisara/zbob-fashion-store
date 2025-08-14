package com.zbob.config;

import com.zbob.modules.blog.entity.BlogPost;
import com.zbob.modules.blog.repository.BlogPostRepository;
import com.zbob.modules.reviews.entity.Review;
import com.zbob.modules.reviews.repository.ReviewRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Sample text so pages can be seen in development. These are not real customers or real reviews. */
@Component
@Profile("dev")
public class DevContentSeeder implements ApplicationRunner {

    private record Post(String title, String topic, int minutes, String excerpt) {
    }

    private static final List<Post> POSTS = List.of(
            new Post("Getting more from a smaller wardrobe", "Style Guide", 6, "Fewer, better pieces mean less time deciding and more outfits that feel right."),
            new Post("How to layer without adding bulk", "Fashion Tips", 4, "A simple order of fabrics keeps you warm and keeps your shape."),
            new Post("Choosing fabric for the way you live", "Style Guide", 5, "What each fibre does well, and which everyday situations suit it best."),
            new Post("Five ways to wear a boxy tee", "Fashion Tips", 3, "One good tee, and five easy outfits to build around it."),
            new Post("Why we design in small batches", "Brand Stories", 5, "How making less helps us get each piece right before it reaches you."),
            new Post("Caring for clothes so they last", "Brand Stories", 7, "A few small washing habits can add years to the things you love."));

    private static final String BODY = """
            ## Start with what you already wear

            Look at the pieces you reach for every week. They tell you more about your real style than anything you bought for a one-off occasion, and they are the best guide to what to buy next.

            - Comfort: choose what you can wear all day without thinking about it
            - Versatility: look for pieces that work with at least three others
            - Quality: pay attention to fabric weight and how the seams are finished

            ## Build around a few good shapes

            Once you know what works, repeat it. A small set of reliable silhouettes in colours that go together makes getting dressed simple and gives everything you own more use.

            ## Look after what you have

            Wash less often, wash colder and always dry knits flat. Small habits like these keep clothes looking their best for far longer, which is the simplest way to get more from them.
            """;

    private final BlogPostRepository posts;
    private final ReviewRepository reviews;

    public DevContentSeeder(BlogPostRepository posts, ReviewRepository reviews) {
        this.posts = posts;
        this.reviews = reviews;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (posts.count() == 0) {
            Instant date = Instant.now();
            for (Post p : POSTS) {
                BlogPost post = new BlogPost(p.title(), com.zbob.common.util.Slugs.of(p.title()));
                post.setExcerpt(p.excerpt());
                post.setBody(BODY);
                post.setTopic(p.topic());
                post.setReadMinutes(p.minutes());
                post.setPublishedAt(date);
                posts.save(post);
                date = date.minus(Duration.ofDays(21));
            }
        }
        if (reviews.count() == 0) {
            sample("Sample Reviewer A", "Demo content", "Replace this sample in the admin dashboard.", "The fabric feels great and the fit is spot on. I keep reaching for it every week.", 5);
            sample("Sample Reviewer B", "Demo content", "Replace this sample in the admin dashboard.", "Simple, well made and easy to wear. It has already become a regular part of my week.", 5);
            sample("Sample Reviewer C", "Demo content", "Replace this sample in the admin dashboard.", "Good weight, clean shape and it has held up well after plenty of washes so far.", 4);
        }
    }

    private void sample(String author, String title, String headline, String body, int rating) {
        Review review = new Review(author, headline, body, rating);
        review.setAuthorTitle(title);
        review.setApproved(true);
        reviews.save(review);
    }
}
