package com.lucabridge.core.media;

import com.lucabridge.core.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/**
 * Reports at startup whether the media bucket actually exists.
 *
 * Without this the first symptom of a missing bucket is a content editor getting
 * "The specified bucket does not exist" from S3 halfway through creating a page —
 * a runtime error, in the CMS, that looks like an application bug rather than an
 * environment one. The bucket is created by the {@code ensure-media-bucket}
 * initContainer (k8s) and the {@code createbuckets} service (compose), so this is
 * a safety net for anything provisioned outside those paths.
 *
 * Deliberately logs rather than throwing: the public site is read-only for
 * visitors and serves perfectly well with storage misconfigured, so refusing to
 * start would turn broken uploads into a site-wide outage. Uploads are the only
 * thing that fails, and they fail loudly on their own.
 */
@Component
public class StorageStartupCheck {

    private static final Logger log = LoggerFactory.getLogger(StorageStartupCheck.class);

    private final S3Client s3Client;
    private final AppProperties appProperties;

    public StorageStartupCheck(S3Client s3Client, AppProperties appProperties) {
        this.s3Client = s3Client;
        this.appProperties = appProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void verifyBucket() {
        String bucket = appProperties.getStorage().getBucket();
        String endpoint = appProperties.getStorage().getEndpoint();

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Media storage ready: bucket '{}' at {}", bucket, endpoint);
        } catch (NoSuchBucketException e) {
            log.error("""
                    Media storage bucket '{}' does not exist at {} — every image upload \
                    will fail with S3 404 until it is created. Fix with:
                      mc alias set local {} <access-key> <secret-key>
                      mc mb --ignore-existing local/{}
                      mc anonymous set download local/{}
                    The second command is required too: media URLs are public links with no \
                    per-object ACL, so images 403 in the browser on a private bucket.""",
                    bucket, endpoint, endpoint, bucket, bucket);
        } catch (RuntimeException e) {
            // Wrong credentials, unreachable endpoint, a proxy in between — all worth
            // surfacing at boot, none of them worth blocking startup over.
            log.error("Could not verify media storage bucket '{}' at {}: {}",
                    bucket, endpoint, e.getMessage());
        }
    }
}
