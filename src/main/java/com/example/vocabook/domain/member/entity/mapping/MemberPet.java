package com.example.vocabook.domain.member.entity.mapping;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.pet.enums.PetStage;
import com.example.vocabook.domain.store.entity.Item;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "member_pet")
public class MemberPet {

	public static final int REQUIRED_EXP = 200;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_pet_id")
	private Long id;

	@Column(name = "current_level", nullable = false)
	@Builder.Default
	private int currentLevel = 1;

	@Column(name = "current_exp", nullable = false)
	@Builder.Default
	private long currentExp = 0L;

	@Column(name = "hunger", nullable = false)
	@Builder.Default
	private int hunger = 50;

	@Column(name = "thirst", nullable = false)
	@Builder.Default
	private int thirst = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_background_item_id")
    private Item activeBackground;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_accessory_item_id")
    private Item activeAccessory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

	public PetStage getStage() {
		return PetStage.computeStage(currentLevel);
	}

	public int feed(int amount) {
		int actual = Math.min(amount, this.hunger);
		this.hunger -= actual;
		return actual;
	}

	public int water(int amount) {
		int actual = Math.min(amount, this.thirst);
		this.thirst -= actual;
		return actual;
	}

	public void gainXp(int amount) {
		this.currentExp += amount;
		if (this.currentExp >= REQUIRED_EXP) {
			long levelUps = this.currentExp / REQUIRED_EXP;
			this.currentLevel += levelUps;
			this.currentExp = this.currentExp % REQUIRED_EXP;
		}
	}

	public void changeBackground(Item bg) {
		this.activeBackground = bg;
	}

	public void changeAccessory(Item accessory) {
		this.activeAccessory = accessory;
	}
}
