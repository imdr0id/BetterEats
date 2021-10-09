package com.imdroid.bettereats.repository;

import com.imdroid.bettereats.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findById(Long reviewId);

    Page<Review> findByCreatedBy(Long userId, Pageable pageable);

    long countByCreatedBy(Long userId);

    List<Review> findByIdIn(List<Long> reviewIds);

    List<Review> findByIdIn(List<Long> reviewIds, Sort sort);
}
