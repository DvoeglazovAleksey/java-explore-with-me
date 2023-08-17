package ru.practicum.dto.compilation;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class NewCompilationDto {
    @NotNull
    @NotBlank
    @Length(min = 1, max = 50)
    private String title;
    private List<Long> events = new ArrayList<>();
    private Boolean pinned = Boolean.FALSE;
}
