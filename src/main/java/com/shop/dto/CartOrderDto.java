package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartOrderDto {
    private List<CartOrderDto> cartOrderDtoList;
    private Long cartItemId;
}
