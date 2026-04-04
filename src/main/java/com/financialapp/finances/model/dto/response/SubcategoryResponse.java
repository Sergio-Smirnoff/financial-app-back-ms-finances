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
public class SubcategoryResponse {
    private Long id;
    private String name;
    private String type;
    @JsonProperty("isSystem")
    private Boolean system;
    private Long userId;
}
