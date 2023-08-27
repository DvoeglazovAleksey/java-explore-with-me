package ru.practicum.dto.comment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class NewCommentDto {
    @NotBlank
    @Size(min = 20, max = 800)
    private String text;
}
