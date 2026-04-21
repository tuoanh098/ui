package com.trohub.backend.dto.billing;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftRequestDto {
    @NotNull(message = "tenantId is required")
    private Long tenantId;
    private Integer nam;
    private Integer thang;
}

