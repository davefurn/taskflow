package com.taskflow.api.dto.request.user;
import com.taskflow.api.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private User.Role role;
}