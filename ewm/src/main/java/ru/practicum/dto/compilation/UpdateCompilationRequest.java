package ru.practicum.dto.compilation;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateCompilationRequest {
    @Length(min = 1, max = 50)
    private String title;
    private Boolean pinned;
    private Set<Long> events = new HashSet<>();
}
