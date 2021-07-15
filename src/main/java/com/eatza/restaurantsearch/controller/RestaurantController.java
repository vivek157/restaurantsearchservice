package com.eatza.restaurantsearch.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eatza.restaurantsearch.model.Restaurant;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.metric.consumer.HealthCountsStream;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eatza.restaurantsearch.dto.RestaurantRequestDto;
import com.eatza.restaurantsearch.dto.RestaurantResponseDto;
import com.eatza.restaurantsearch.exception.RestaurantBadRequestException;
import com.eatza.restaurantsearch.exception.RestaurantNotFoundException;
import com.eatza.restaurantsearch.model.MenuItem;
import com.eatza.restaurantsearch.service.restaurantservice.RestaurantService;


@RestController
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    private static final String RESTAURANT_BAD_REQUEST_MSG = "Page number or Page size cannot be 0 or less";
    private static final String RESTAURANT_NOT_FOUND_MSG = "No Restaurants found for specified inputs";

    /*@HystrixCommand(fallbackMethod = "fallBackRestaurant",commandKey = "getAllRestaurants",groupKey = "getAllRestaurants")*/
    @GetMapping("/restaurants")
    public ResponseEntity<RestaurantResponseDto> getAllRestaurants(@RequestHeader String authorization, @RequestParam(defaultValue = "1") int pagenumber, @RequestParam(defaultValue = "10") int pagesize) {
        logger.debug("In getall restaurants method");
        if (pagenumber <= 0 || pagesize <= 0) {
            logger.debug("Page number or size cannot be zero or less, throwing exception");
            throw new RestaurantBadRequestException(RESTAURANT_BAD_REQUEST_MSG);
        }
        logger.debug("calling service to get restaurants with pagination");
        RestaurantResponseDto responseDto = restaurantService.findAllRestaurants(pagenumber, pagesize);
        if (responseDto.getRestaurants().isEmpty()) {
            logger.debug("No restaurants were found");
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MSG);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);


    }
/*    public ResponseEntity<RestaurantResponseDto> fallBackRestaurant(){
  *//*      List<Restaurant> resturants = Arrays.asList(new Restaurant("Resturant1", "Location1", "Chinese", 400, 4.0)
                , new Restaurant("Resturant2", "Location2", "Indian", 200, 4.3)
                , new Restaurant("Resturant3", "Location3", "South Indian", 300, 4.6));

        RestaurantResponseDto responseDTO = new RestaurantResponseDto(resturants, 1, 10);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDTO);*//*

        throw new RestaurantNotFoundException("FALLBACK -------");
    }*/

    @PostMapping("/restaurant")
    public ResponseEntity<String> addRestaurant(@RequestHeader String authorization, @RequestBody RestaurantRequestDto restaurantDto) {

        logger.debug("In add restaurants method, calling service");

        restaurantService.saveRestaurant(restaurantDto);
        logger.debug("Restaurant saved, returning back");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Restaurant Added successfully");


    }

    /// for hystrix
    @HystrixCommand(fallbackMethod = "fallBackHello", commandKey = "Hello", groupKey = "Hello")
    @GetMapping("/hello")
    public String hello() {
        if (RandomUtils.nextBoolean()) {
            throw new RuntimeException("Failed Hello");
        }
        return "Hello Restaurant";
    }

    public String fallBackHello() {
        return "FallBackHello Initiated";
    }

    @HystrixCommand(fallbackMethod = "fallBackHelloSecond", commandKey = "HelloSecond", groupKey = "Hello2222"
           /* commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "500"),
                    @HystrixProperty(name="hystrix.command.default.circuitBreaker.requestVolumeThreshold",value="5")
            },
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "30"),
                    @HystrixProperty(name = "maxQueueSize", value = "101"),
                    @HystrixProperty(name = "keepAliveTimeMinutes", value = "2"),
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "5"),
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "12"),
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000")
            }*/)
    @GetMapping("/hello2222")
    public String helloSecond() {
        boolean value = RandomUtils.nextBoolean();
        logger.info("##### hello2222 boolean :{}", value);
        if (value) {
            throw new RuntimeException("Failed Hello 22222");
        }
        return "Hello2222 Restaurant";
    }

    public String fallBackHelloSecond() {
        return "fallBackHelloSecond2222 Initiated";
    }

    @GetMapping("/reset/circuitBreaker")
    public String resetCircuitBreaker(){
        Hystrix.reset();
      // HystrixCommandKey commandKey= Hystrix.getCurrentThreadExecutingCommand();
        HealthCountsStream.reset();
        return "reset done";
    }


/////

    @GetMapping("/restaurants/name/{name}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantsByName(@RequestHeader String authorization, @PathVariable String name, @RequestParam(defaultValue = "1") int pagenumber, @RequestParam(defaultValue = "10") int pagesize) {
        logger.debug("In get restaurants by name method");
        if (pagenumber <= 0 || pagesize <= 0) {
            logger.debug("Page number or size cannot be zero or less, throwing exception");

            throw new RestaurantBadRequestException(RESTAURANT_BAD_REQUEST_MSG);
        }
        RestaurantResponseDto responseDto = restaurantService.findByName(name, pagenumber, pagesize);
        if (responseDto.getRestaurants().isEmpty()) {
            logger.debug("No restaurants were found");

            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MSG);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @GetMapping("/restaurants/location/{location}/cuisine/{cuisine}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantsByLocationCuisine(@RequestHeader String authorization, @PathVariable String location, @PathVariable String cuisine, @RequestParam(defaultValue = "1") int pagenumber, @RequestParam(defaultValue = "10") int pagesize) {
        logger.debug("In get restaurants by location and cuisine method");
        if (pagenumber <= 0 || pagesize <= 0) {
            logger.debug("Page number or size cannot be zero or less, throwing exception");
            throw new RestaurantBadRequestException(RESTAURANT_BAD_REQUEST_MSG);
        }
        RestaurantResponseDto responseDto = restaurantService.findByLocationAndCuisine(location, cuisine, pagenumber, pagesize);
        if (responseDto.getRestaurants().isEmpty()) {
            logger.debug("No restaurants were found");
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MSG);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);

    }

    @GetMapping("/restaurants/name/{name}/location/{location}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantsByLocationName(@RequestHeader String authorization, @PathVariable String location, @PathVariable String name, @RequestParam(defaultValue = "1") int pagenumber, @RequestParam(defaultValue = "10") int pagesize) {
        logger.debug("In get restaurants by location and cuisine method");

        if (pagenumber <= 0 || pagesize <= 0) {
            logger.debug("Page number or size cannot be zero or less, throwing exception");
            throw new RestaurantBadRequestException(RESTAURANT_BAD_REQUEST_MSG);
        }
        RestaurantResponseDto responseDto = restaurantService.findByLocationAndName(location, name, pagenumber, pagesize);
        if (responseDto.getRestaurants().isEmpty()) {
            logger.debug("No restaurants were found");
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MSG);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);

    }

    @GetMapping("/restaurants/budget/{budget}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantsByBudget(@RequestHeader String authorization, @PathVariable int budget, @RequestParam(defaultValue = "1") int pagenumber, @RequestParam(defaultValue = "10") int pagesize) {
        logger.debug("In get restaurants by budget method");

        if (pagenumber <= 0 || pagesize <= 0) {
            logger.debug("Page number or size cannot be zero or less, throwing exception");
            throw new RestaurantBadRequestException(RESTAURANT_BAD_REQUEST_MSG);
        }
        RestaurantResponseDto responseDto = restaurantService.findByBudget(budget, pagenumber, pagesize);
        if (responseDto.getRestaurants().isEmpty()) {
            logger.debug("No restaurants were found");
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MSG);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);

    }

    @GetMapping("/restaurants/rating/{rating}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantsByRating(@RequestHeader String authorization, @PathVariable double rating, @RequestParam(defaultValue = "1") int pagenumber, @RequestParam(defaultValue = "10") int pagesize) {
        logger.debug("In get restaurants by rating method");

        if (pagenumber <= 0 || pagesize <= 0) {
            logger.debug("Page number or size cannot be zero or less, throwing exception");
            throw new RestaurantBadRequestException(RESTAURANT_BAD_REQUEST_MSG);
        }
        RestaurantResponseDto responseDto = restaurantService.findByRating(rating, pagenumber, pagesize);
        if (responseDto.getRestaurants().isEmpty()) {
            logger.debug("No restaurants were found");
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MSG);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);

    }

    @GetMapping("/restaurant/items/{restaurantid}")
    public ResponseEntity<List<MenuItem>> getItemsByRestaurantId(@RequestHeader String authorization, @PathVariable Long restaurantid, @RequestParam(defaultValue = "1") int pagenumber, @RequestParam(defaultValue = "10") int pagesize) {

        List<MenuItem> items = restaurantService.findMenuItemByRestaurantId(restaurantid, pagenumber, pagesize);
        if (items.isEmpty()) {
            throw new RestaurantNotFoundException(RESTAURANT_NOT_FOUND_MSG);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(items);

    }
}