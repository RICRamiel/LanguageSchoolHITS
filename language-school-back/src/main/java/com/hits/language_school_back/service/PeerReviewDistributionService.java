package com.hits.language_school_back.service;

import com.hits.language_school_back.model.Task;

public interface PeerReviewDistributionService {
    void createDistributionIfReady(Task task);
}
