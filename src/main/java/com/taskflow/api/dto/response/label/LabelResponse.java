package com.taskflow.api.dto.response.label;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class LabelResponse {
    private UUID id;
    private String name;
    private String colour;
}