package com.example.vocabook.domain.store.entity;

import com.example.vocabook.domain.store.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "item")
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_id")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "price", nullable = false)
	@Builder.Default
	private Long price = 0L;

    @Column(name = "image_url", nullable = false)
    @Builder.Default
    private String imageUrl = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
