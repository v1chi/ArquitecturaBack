package com.team.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public class ChangeNameRequest {
    @NotBlank
    @Size(min = 2, max = 150)
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Full name must contain only letters and spaces")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
