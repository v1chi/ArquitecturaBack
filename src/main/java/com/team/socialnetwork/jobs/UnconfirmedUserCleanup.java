package com.team.socialnetwork.jobs;

import com.team.socialnetwork.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class UnconfirmedUserCleanup {
    private static final Logger log = LoggerFactory.getLogger(UnconfirmedUserCleanup.class);

    private final UserRepository userRepository;

    public UnconfirmedUserCleanup(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Runs periodically; interval configurable via cleanup.unconfirmedUser.fixedDelay
    @Scheduled(fixedDelayString = "${cleanup.unconfirmedUser.fixedDelay:600000}")
    public void deleteUnconfirmedOlderThanOneHour() {
        Instant threshold = Instant.now().minus(1, ChronoUnit.HOURS);
        long deleted = userRepository.deleteByEmailConfirmedFalseAndCreatedAtBefore(threshold);
        if (deleted > 0) {
            log.info("Deleted {} unconfirmed users older than 1 hour", deleted);
        }
    }
}

