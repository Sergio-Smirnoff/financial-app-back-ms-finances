package com.financialapp.finances.model.dto.request;

import com.financialapp.finances.model.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateParentCategoryRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Type is required")
    private CategoryType type;

    @Size(max = 7, message = "Color must be a valid hex code (max 7 chars)")
    private String color;

    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    private String icon;
}
