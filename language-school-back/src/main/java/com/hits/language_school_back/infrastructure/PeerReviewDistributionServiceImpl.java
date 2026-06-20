package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.service.PeerReviewDistributionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PeerReviewDistributionServiceImpl implements PeerReviewDistributionService {
    @Override
    public void createDistributionIfReady(Task task) {
        if (task == null || !Boolean.TRUE.equals(task.getPeerReviewEnabled())) {
            return;
        }
        if (!Boolean.TRUE.equals(task.getSubmissionClosed())) {
            log.debug("Peer-review distribution for task {} is deferred until submissions are closed", task.getId());
            return;
        }

        log.debug("Peer-review distribution dispatch is ready for task {} with type {}", task.getId(), task.getPeerReviewDistributionType());
    }
}
