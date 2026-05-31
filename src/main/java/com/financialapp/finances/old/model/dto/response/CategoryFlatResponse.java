package com.financialapp.finances.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryFlatResponse {
    private Long id;
    private Long parentId;
    private Long userId;
    private String name;
    private String type;
    private String color;
    private String icon;
    @JsonProperty("isSystem")
    private Boolean system;
    private Boolean active;
}
