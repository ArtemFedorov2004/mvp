package io.github.artemfedorov2004.onlinestoreservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(schema = "online_store", name = "t_review")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "c_rating")
    private Integer rating;

    @ManyToOne
    @JoinTable(
            name = "t_product_review",
            schema = "online_store",
            joinColumns = @JoinColumn(
                    name = "id_review"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "id_product"
            )
    )
    private Product forProduct;

    @ManyToOne
    @JoinTable(
            name = "t_customer_review",
            schema = "online_store",
            joinColumns = @JoinColumn(
                    name = "id_review"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "id_customer"
            )
    )
    private Customer createdBy;

    @Column(name = "c_created_at")
    private LocalDateTime createdAt;

    @Column(name = "c_advantages")
    private String advantages;

    @Column(name = "c_disadvantages")
    private String disadvantages;

    @Column(name = "c_comment")
    private String comment;
}
