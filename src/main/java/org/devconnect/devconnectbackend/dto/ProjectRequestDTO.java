package org.devconnect.devconnectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRequestDTO {
    private String projectName;
    private Long devId;
    private Long clientId;
    private String description;
    private BigDecimal projectBudget;
    private LocalDateTime timeline;
}
