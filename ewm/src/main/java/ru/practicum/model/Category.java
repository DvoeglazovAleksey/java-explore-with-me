package ru.practicum.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "categories")
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}
