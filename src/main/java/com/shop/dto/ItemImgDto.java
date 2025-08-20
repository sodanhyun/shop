package com.shop.dto;

import com.shop.entity.ItemImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class ItemImgDto {

    private Long id;
    private String imgName;
    private String oriImgName;
    private String imgUrl;
    private String repImgYn;

    private static ModelMapper modelMapper = new ModelMapper();

    public static ItemImgDto of(ItemImg itemImg) {
        // ModelMapper mapper = new ModelMapper();
        // modelMapper를 사용해서 엔티티 to DTO 변환된 객체 반환
        return modelMapper.map(itemImg, ItemImgDto.class);
    }

}
