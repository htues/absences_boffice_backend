package com.hftamayo.absencesbobe.shared.infrastructure.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    private final SeedProperties props;
    private final List<SeedTask> tasks;

    public SeedRunner(SeedProperties props, List<SeedTask> tasks) {
        this.props = props;
        this.tasks = tasks;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!props.enabled()) {
            log.info("Seeding disabled (seed.enabled=false)");
            return;
        }

        List<String> includeList = props.include() == null ? List.of() : props.include();
        Set<String> include = Set.copyOf(includeList);

        log.info("Seeding enabled. include={}, failFast={}", includeList, props.failFast());

        tasks.stream()
                .sorted(Comparator.comparingInt(SeedTask::order))
                .filter(t -> include.isEmpty() || include.contains(t.id()))
                .forEach(task -> {
                    log.info("Running seed task: {}", task.id());
                    try {
                        task.run();
                        log.info("Seed task completed: {}", task.id());
                    } catch (RuntimeException ex) {
                        log.error("Seed task failed: {}", task.id(), ex);
                        if (props.failFast()) {
                            throw ex; // fail startup cleanly, no System.exit
                        }
                    }
                });
    }
}