package com.imdroid.bettereats.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.imdroid.bettereats.payload.RestaurantResponse;
import com.imdroid.bettereats.service.EntreeService;
import com.imdroid.bettereats.util.AppConstants;

@RestController
@RequestMapping("/api/search")
public class SearchController {

	@Autowired
	private EntreeService entreeService;

	@GetMapping
	public List<RestaurantResponse> getResults(@RequestParam(value = "search") String strSearch,
			@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

		
		 return  entreeService.search(strSearch, page, size);

	}

}
