package ru.practicum.dto.compilation;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateCompilationRequest {
    @Length(min = 1, max = 50)
    private String title;
    private Boolean pinned;
    private List<Long> events = new ArrayList<>();
}
