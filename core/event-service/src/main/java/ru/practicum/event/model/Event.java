package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.category.model.Category;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;

    @Embedded
    private Location location;

    @JoinColumn(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
