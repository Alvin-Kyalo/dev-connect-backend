package org.devconnect.devconnectbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String imageUrl;
}
