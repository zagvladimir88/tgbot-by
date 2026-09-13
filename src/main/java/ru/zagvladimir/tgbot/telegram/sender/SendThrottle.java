package ru.zagvladimir.tgbot.telegram.sender;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendThrottle {

    private static final Logger log = LoggerFactory.getLogger(SendThrottle.class);

    private static final int GLOBAL_MESSAGES_PER_SECOND = 25;
    private static final int MESSAGES_PER_CHAT_PER_MINUTE = 18;

    private final Bucket globalBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(
                    GLOBAL_MESSAGES_PER_SECOND, Refill.greedy(GLOBAL_MESSAGES_PER_SECOND, Duration.ofSeconds(1))))
            .build();

    private final ConcurrentMap<Long, Bucket> chatBuckets = new ConcurrentHashMap<>();

    public void acquire(long chatId) {
        try {
            globalBucket.asBlocking().consume(1);
            chatBucket(chatId).asBlocking().consume(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Ожидание слота отправки в чат {} прервано", chatId);
        }
    }

    private Bucket chatBucket(long chatId) {
        return chatBuckets.computeIfAbsent(chatId, id -> Bucket.builder()
                .addLimit(Bandwidth.classic(
                        MESSAGES_PER_CHAT_PER_MINUTE,
                        Refill.greedy(MESSAGES_PER_CHAT_PER_MINUTE, Duration.ofMinutes(1))))
                .build());
    }
}
