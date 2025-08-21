package com.lionkit.mogumarket.review.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewUpdateRequest {
    private Integer rating; // 1~5
}