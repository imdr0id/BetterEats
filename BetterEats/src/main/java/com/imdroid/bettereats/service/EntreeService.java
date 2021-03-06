package com.imdroid.bettereats.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.imdroid.bettereats.exception.BadRequestException;
import com.imdroid.bettereats.exception.ResourceNotFoundException;
import com.imdroid.bettereats.model.Entree;
import com.imdroid.bettereats.model.Restaurant;
import com.imdroid.bettereats.model.Review;
import com.imdroid.bettereats.model.ReviewToUser;
import com.imdroid.bettereats.model.User;
import com.imdroid.bettereats.payload.EntreeResponse;
import com.imdroid.bettereats.payload.PagedResponse;
import com.imdroid.bettereats.payload.RestaurantResponse;
import com.imdroid.bettereats.payload.ReviewRequest;
import com.imdroid.bettereats.payload.ReviewResponse;
import com.imdroid.bettereats.repository.EntreeRepository;
import com.imdroid.bettereats.repository.ReviewRepository;
import com.imdroid.bettereats.repository.ReviewToUserRepository;
import com.imdroid.bettereats.repository.UserRepository;
import com.imdroid.bettereats.security.UserPrincipal;
import com.imdroid.bettereats.util.AppConstants;
import com.imdroid.bettereats.util.ModelMapper;

@Service
public class EntreeService {

	@Autowired
	private EntreeRepository entreeRepository;

	@Autowired
	private ReviewToUserRepository reviewToUserRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private UserRepository userRepository;

	@PersistenceContext
	private EntityManager entityManager;

	
	@Transactional
	public List<RestaurantResponse> search(String searchText, int pageNo, int resultsPerPage) {

		FullTextQuery jpaQuery = searchUsersQuery(searchText);

		jpaQuery.setMaxResults(resultsPerPage);
		jpaQuery.setFirstResult((pageNo) * resultsPerPage);

		List<Entree> entreeList = jpaQuery.getResultList();
		
		List<RestaurantResponse> resLIst= new ArrayList<RestaurantResponse>();
		for(Entree en : entreeList) {
			RestaurantResponse r = new RestaurantResponse();
			r.setId(en.getRestaurant().getId());
			r.setName(en.getRestaurant().getName());
			r.setDescription(en.getRestaurant().getDescription());
			resLIst.add(r);
		}

		return resLIst;
	}

	private FullTextQuery searchUsersQuery(String searchText) {

		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Entree.class)
				.get();

		org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().wildcard()
				.onFields("name", "description", "restaurant.name").boostedTo(5f).andField("restaurant.description")
				.matching(searchText + "*").createQuery();

		FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Entree.class);
		
		return jpaQuery;
	}

	private static final Logger logger = LoggerFactory.getLogger(EntreeService.class);

	public PagedResponse<EntreeResponse> getAllEntrees(UserPrincipal currentUser, int page, int size) {
		validatePageNumberAndSize(page, size);

		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Entree> entrees = entreeRepository.findAll(pageable);

		if (entrees.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), entrees.getNumber(), entrees.getSize(),
					entrees.getTotalElements(), entrees.getTotalPages(), entrees.isLast());
		}

		Map<Long, User> creatorMap = getEntreeCreatorMap(entrees.getContent());

		List<EntreeResponse> entreeResponses = entrees.map(entree -> {
			Map<Long, User> reviewCreatorMap = getReviewCreatorMap(entree.getReviews());
			return ModelMapper.mapEntreeToEntreeResponse(entree, creatorMap.get(entree.getCreatedBy()),
					reviewCreatorMap);
		}).getContent();

		return new PagedResponse<>(entreeResponses, entrees.getNumber(), entrees.getSize(), entrees.getTotalElements(),
				entrees.getTotalPages(), entrees.isLast());
	}

	public PagedResponse<ReviewResponse> getReviewsCreatedBy(String username, UserPrincipal currentUser, int page,
			int size) {
		validatePageNumberAndSize(page, size);

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Review> userReviewIds = reviewRepository.findByCreatedBy(user.getId(), pageable);

		if (userReviewIds.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), userReviewIds.getNumber(), userReviewIds.getSize(),
					userReviewIds.getTotalElements(), userReviewIds.getTotalPages(), userReviewIds.isLast());
		}

		List<Review> reviews = userReviewIds.getContent();

//        Sort sort = new Sort(Sort.Direction.DESC, "createdAt");
//        List<Review> reviews = reviewRepository.findByIdIn(reviewIds, sort);

		Map<Long, User> creatorMap = getReviewCreatorMap(reviews);

		List<ReviewResponse> reviewResponses = reviews.stream().map(review -> {
			return ModelMapper.mapReviewToReviewResponse(review, creatorMap.get(review.getCreatedBy()));
		}).collect(Collectors.toList());

		return new PagedResponse<>(reviewResponses, userReviewIds.getNumber(), userReviewIds.getSize(),
				userReviewIds.getTotalElements(), userReviewIds.getTotalPages(), userReviewIds.isLast());
	}

	public EntreeResponse getEntreeById(Long pollId, UserPrincipal currentUser) {
		Entree poll = entreeRepository.findById(pollId)
				.orElseThrow(() -> new ResourceNotFoundException("Entree", "id", pollId));

		// Retrieve poll creator details
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));

		// Retrieve vote done by logged in user
		ReviewToUser userVote = null;
		if (currentUser != null) {
			userVote = reviewToUserRepository.findByUserIdAndEntreeId(currentUser.getId(), pollId);
		}

		return ModelMapper.mapEntreeToEntreeResponse(poll, creator, getReviewCreatorMap(poll.getReviews()));
	}

	public EntreeResponse addReview(Long entreeId, ReviewRequest reviewRequest, UserPrincipal currentUser) {
		Entree entree = entreeRepository.findById(entreeId)
				.orElseThrow(() -> new ResourceNotFoundException("Entree", "id", entreeId));

		User user = userRepository.getOne(currentUser.getId());

		Review review = new Review(reviewRequest.getText(), reviewRequest.getRating(), entree);

		try {
			review = reviewRepository.save(review);
		} catch (DataIntegrityViolationException ex) {
			logger.info("User {} has already has a review in Entree {}", currentUser.getId(), entreeId);
			throw new BadRequestException("Sorry! You have already added a review to this entree.");
		}

		// Retrieve poll creator details
		User creator = userRepository.findById(entree.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", entree.getCreatedBy()));

		return ModelMapper.mapEntreeToEntreeResponse(entree, creator, getReviewCreatorMap(entree.getReviews()));
	}

	private void validatePageNumberAndSize(int page, int size) {
		if (page < 0) {
			throw new BadRequestException("Page number cannot be less than zero.");
		}

		if (size > AppConstants.MAX_PAGE_SIZE) {
			throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
		}
	}

	Map<Long, User> getEntreeCreatorMap(List<Entree> entrees) {
		// Get Poll Creator details of the given list of polls
		List<Long> creatorIds = entrees.stream().map(Entree::getCreatedBy).distinct().collect(Collectors.toList());

		List<User> creators = userRepository.findByIdIn(creatorIds);
		Map<Long, User> creatorMap = creators.stream().collect(Collectors.toMap(User::getId, Function.identity()));

		return creatorMap;
	}

	Map<Long, User> getReviewCreatorMap(List<Review> reviews) {
		List<Long> creatorIds = reviews.stream().map(Review::getCreatedBy).distinct().collect(Collectors.toList());

		List<User> creators = userRepository.findByIdIn(creatorIds);
		Map<Long, User> creatorMap = creators.stream().collect(Collectors.toMap(User::getId, Function.identity()));

		return creatorMap;
	}

}
